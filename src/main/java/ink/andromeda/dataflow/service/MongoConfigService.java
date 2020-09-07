package ink.andromeda.dataflow.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import ink.andromeda.dataflow.service.event.DefaultEventService;
import ink.andromeda.dataflow.service.event.ProductizationConfigService;
import ink.andromeda.dataflow.service.event.ProductizationEventService;
import ink.andromeda.dataflow.service.sync.CoreTableSyncService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ink.andromeda.dataflow.util.JSONConfigValidator;
import net.abakus.coresystem.redis.RedisClient;
import org.apache.commons.lang.IncompleteArgumentException;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPubSub;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import static net.abakus.coresystem.redis.RedisMessageChannels.REFRESH_EVENT_MODULE_CONFIG;

/**
 * mongo配置文件的相关操作服务
 */
@Service
@Slf4j
public class MongoConfigService {

    private final MongoTemplate mongoTemplate;

    private final DefaultEventService defaultEventService;

    private final ProductizationEventService productizationEventService;

    private final ProductizationConfigService productizationConfigService;

    private final CoreTableSyncService syncService;

    private final ExpressionService expressionService;

    private final RedisClient redisClient;

    public static final String EVENT_COLLECTION_NAME = "event_config";

    public static final String SYNC_COLLECTION_NAME = "sync_config";

    public static final String TEMPLATE_COLLECTION_NAME = "template_config";

    @Getter
    private final Map<String, JSONConfigValidator> configValidator = new ConcurrentHashMap<>();

    public MongoConfigService(MongoTemplate mongoTemplate,
                              DefaultEventService defaultEventService,
                              ProductizationEventService productizationEventService,
                              ProductizationConfigService productizationConfigService,
                              CoreTableSyncService syncService,
                              ExpressionService expressionService,
                              RedisClient redisClient) {
        this.mongoTemplate = mongoTemplate;
        this.defaultEventService = defaultEventService;
        this.productizationEventService = productizationEventService;
        this.productizationConfigService = productizationConfigService;
        this.syncService = syncService;
        this.expressionService = expressionService;
        this.redisClient = redisClient;
        refreshTemplateConfig();
    }

    @PostConstruct
    public void init(){
        Executors.newSingleThreadExecutor().submit(() -> redisClient.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                log.info("on redis message channel: {}, message: {}", channel, message);
                switch (message){
                    case "sync-event-config":
                        int i = expireLocalCache(null);
                        log.info("remove {} cache item", i);
                        break;
                    case "event-config":
                        expireLocalCache("event");
                }
            }

            @Override
            public void onSubscribe(String channel, int subscribedChannels) {
                log.info("on subscribe redis message channel: {}, count: {}", channel, subscribedChannels);
            }

        }, REFRESH_EVENT_MODULE_CONFIG));

    }

    public JSONObject getConfigTemplate(String templateId) {
        List<JSONObject> list = mongoTemplate
                .find(new Query(Criteria.where("_id").is(templateId)), JSONObject.class, TEMPLATE_COLLECTION_NAME);
        if (list.isEmpty())
            throw new RuntimeException("could not found config template: " + templateId);
        return list.get(0).getJSONObject("template");
    }

    private void refreshTemplateConfig() {
        mongoTemplate.getCollection(TEMPLATE_COLLECTION_NAME).find().forEach((Consumer<? super Document>) document ->
                configValidator.put(document.getString("_id"), new JSONConfigValidator(JSON.parseObject(JSON.toJSONString(document.get("template")))))
        );
    }

    public JSONObject getConfig(String schema, String table, String collection) {
        return Optional.ofNullable(mongoTemplate.findOne(Query.query(Criteria.where("schema").is(schema))
                .addCriteria(Criteria.where("table").is(table)), JSONObject.class, collection)).orElseThrow(
                () -> new IllegalArgumentException(String.format("collection '%s' not found or could not found config '%s.%s'", collection, schema, table))
        );
    }

    /**
     * 清空配置文件缓存
     *
     * @param cacheType 可选: sync, event, template; 若为null则删除全部
     */
    public int expireLocalCache(@Nullable String cacheType) {
        int result;
        expressionService.refreshColumnInfoCache();
        if (StringUtils.isEmpty(cacheType)) {
            result = defaultEventService.expireCache()
                     + syncService.expireCache()
                     + configValidator.size()
                     + productizationEventService.expireCache();
            productizationConfigService.refreshCache();
            refreshTemplateConfig();
        } else {
            switch (cacheType) {
                case "sync":
                    result = syncService.expireCache();
                    break;
                case "event":
                    result = defaultEventService.expireCache();
                    break;
                case "template":
                    result = configValidator.size();
                    refreshTemplateConfig();
                    break;
                default:
                    throw new IncompleteArgumentException("unknown cache type: " + cacheType);
            }
        }
        return result;

    }

    public void expireCache(){
        redisClient.publish(REFRESH_EVENT_MODULE_CONFIG, "sync-event-config");
        log.info("publish redis message, channel: {}, message: {}", REFRESH_EVENT_MODULE_CONFIG, "sync-event-config");
    }

    /**
     * 更新配置
     *
     * @param collection 集合名称
     * @param newConfig  更新的配置
     */
    public long updateConfig(String collection, JSONObject newConfig) {
        // List<String> error = validateConfig(collectionName, newConfig, true);
//        if (error.size() > 0)
//            return HttpResult.FAILED(error.toString());
        Document query = getQuery(collection, newConfig);
        newConfig.remove("_id");
        return mongoTemplate.getCollection(collection).replaceOne(query, Document.parse(newConfig.toJSONString())).getModifiedCount();
    }

    /**
     * 新增配置
     *
     * @param collection 集合名称
     * @param config     配置
     */
    public boolean newConfig(String collection, JSONObject config) {
//        List<String> error = validateConfig(collection, config, true);
//        if (error.size() > 0)
//            return HttpResult.FAILED(error.toString());
        Document query = getQuery(collection, config);
        if (mongoTemplate.getCollection(collection).find(query, JSONObject.class).first() != null) {
            throw new IllegalStateException(
                    String.format("collection '%s' has exists: %s", collection, query));
        }
        mongoTemplate.insert(config, collection);
        return true;
    }

    private Document getQuery(String collection, JSONObject config) {
        Document query = new Document();
        Document document = Document.parse(config.toJSONString());


        switch (collection) {
            case "event_config":
            case "sync_config":
                String schema = Objects.requireNonNull(config.getString("schema"), "schema name could not be null");
                String table = Objects.requireNonNull(config.getString("table"), "table name could not be null");
                query.put("schema", schema);
                query.put("table", table);
                document.remove("_id");
                break;
            case "history_data_config":
                String source = Objects.requireNonNull(config.getString("_id"), "_id(source) could not be null");
                query.put("_id", source);
                break;
            case "template_config":
                String templateCollection = Objects.requireNonNull(config.getString("_id"), "_id(collection) could not be null");
                query.put("_id", templateCollection);
                break;
            default:
                throw new IllegalArgumentException("unknown collection " + collection);

        }
        return query;
    }

    public int deleteConfig(String schema, String table, String collectionName) {
        return mongoTemplate.findAllAndRemove(new Query(Criteria.where("schema").is(schema))
                .addCriteria(Criteria.where("table").is(table)), collectionName).size();
    }

    public void newTemplateConfig(JSONObject config) {
        mongoTemplate.insert(config, "datachannel_config_template");
    }

    public int updateTemplateConfig(JSONObject config) {
        String templateId = Objects.requireNonNull(config.getString("_id"), "_id could not be null");
        Document query = new Document();
        query.put("_id", templateId);
        return (int) mongoTemplate.getCollection(TEMPLATE_COLLECTION_NAME).updateOne(query, Document.parse(config.toJSONString())).getModifiedCount();
    }

    /**
     * 校验配置文件
     *
     * @param collectionName 配置文件的集合名
     * @param config         要校验的配置
     * @return 错误信息, 为空则代表校验通过
     */
    @NonNull
    public List<String> validateConfig(String collectionName, JSONObject config, boolean ignoreTemplateNotExist) {
        JSONConfigValidator validator = configValidator.get(collectionName);
        if (validator == null) {
            if (ignoreTemplateNotExist)
                return Collections.emptyList();
            throw new RuntimeException("could not found collection [" + collectionName + "] template!");
        }
        return validator.validate(config);
    }

    public static final Pattern SQUARE_BRACKET_REGEX = Pattern.compile("(?<=\\[)(.+?)(?=])");

    /**
     * 获取配置的元数据
     *
     * @return collections: 可选集合名称, nameRefs: 每个数据库下可选表名称, sources: 可选source名称
     */
    public Map<String, Object> getConfigMetaData() {
        Map<String, Object> object = new HashMap<>();
        String[] collections = {"sync_config", "event_config", "history_data_config"};
        object.put("collections", collections);
        Map<String, List<String>> nameRefs = new HashMap<>();
        object.put("nameRefs", nameRefs);
        mongoTemplate.getCollection("sync_config").find().forEach((Consumer<? super Document>) document -> {
            // String _id = document.getString("_id");
            // Matcher matcher = SQUARE_BRACKET_REGEX.matcher(_id);
            String schemaName = document.getString("schema");
            String tableName = document.getString("table");
            // int i = 0;
            // while (matcher.find()) {
            //     if (i++ == 0)
            //         schemaName = matcher.group();
            //     else
            //         tableName = matcher.group();
            // }
            nameRefs.computeIfAbsent(schemaName, k -> new ArrayList<>()).add(tableName);
        });
        List<String> sources = new ArrayList<>();
        mongoTemplate.getCollection("history_data_config").find().map(document -> document.getString("_id").trim())
                .forEach((Consumer<? super String>) sources::add);
        object.put("sources", sources);
        return object;
    }
}
