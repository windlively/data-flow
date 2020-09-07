package ink.andromeda.dataflow.service.sync;

import ink.andromeda.dataflow.service.ExpressionService;
import ink.andromeda.dataflow.service.ThreadPoolService;
import ink.andromeda.dataflow.service.event.ProductizationEventService;
import ink.andromeda.dataflow.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import ink.andromeda.dataflow.datasource.dao.AutoConfigurableCoreTableDao;
import ink.andromeda.dataflow.datasource.dao.CoreTableDao;
import ink.andromeda.dataflow.entity.SourceEntity;
import ink.andromeda.dataflow.service.ApplicationEventService;
import org.bson.Document;
import org.slf4j.MDC;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static ink.andromeda.dataflow.service.MongoConfigService.SYNC_COLLECTION_NAME;
import static ink.andromeda.dataflow.util.CommonUtils.randomId;


/**
 * 数据库表同步服务
 */
@Service
@Slf4j
public class CoreTableSyncService {

    // 核心库表名与实体类映射关系
    // public final static Map<String, Class<? extends CoreBean>> CORE_BEAN_TYPE_REF;

    // 核心库对应的实体类默认包名
    // private final static String DEFAULT_CORE_CLASSPATH_PREFIX = "net.wecash.coresystem.data.entity.core";

    private final AutoConfigurableCoreTableDao autoConfigurableCoreTableDao;

    private final CoreTableDao coreTableDao;

    private final ProductizationEventService productizationEventService;

    private final MongoTemplate mongoTemplate;

    private final ApplicationEventService applicationEventService;

    static {
        /*
            Reflections reflections = new Reflections(DEFAULT_CORE_CLASSPATH_PREFIX);
            CORE_BEAN_TYPE_REF = new HashMap<>();
            for (Class<? extends CoreBean> clazz : reflections.getSubTypesOf(CoreBean.class)) {
                String className = clazz.getSimpleName();
                if (className.endsWith("Bean"))
                    className = className.replace("Bean", "");
                String tableName = CommonUtils.camelCaseToUpCase(className);
                CORE_BEAN_TYPE_REF.put(tableName, clazz);
            }
            log.debug("load CORE_BEAN_TYPE_REF: {}", CommonUtils.toJSONString(CORE_BEAN_TYPE_REF));
        */
    }

    private final ExpressionService expressionService;

    public CoreTableSyncService(AutoConfigurableCoreTableDao autoConfigurableCoreTableDao,
                                CoreTableDao coreTableDao,
                                ProductizationEventService productizationEventService,
                                MongoTemplate mongoTemplate,
                                ApplicationEventService applicationEventService,
                                ExpressionService expressionService) {
        this.autoConfigurableCoreTableDao = autoConfigurableCoreTableDao;
        this.coreTableDao = coreTableDao;
        this.productizationEventService = productizationEventService;
        this.mongoTemplate = mongoTemplate;
        this.applicationEventService = applicationEventService;
        this.expressionService = expressionService;
    }


    public void coreTableSync(SourceEntity sourceEntity, boolean pushEvent) {
        String schemaName = sourceEntity.getSchema();
        String tableName = sourceEntity.getName();
        getConverter(schemaName, tableName).forEach((id, converter) -> {
            ThreadPoolService.CS_SYNC_TASK_GROUP()
                    .submit(new SyncTask(converter, sourceEntity, pushEvent ? productizationEventService : null, applicationEventService));
        });
    }

    public void coreTableSync(List<SourceEntity> businessEntities, boolean pushEvent) {
        if (CollectionUtils.isEmpty(businessEntities))
            return;
        businessEntities.forEach(businessEntity -> {
            MDC.put("traceId", randomId());
            getConverter(businessEntity.getSchema(), businessEntity.getName())
                    .forEach((id, converter) -> {
                        new SyncTask(converter, businessEntity, pushEvent ? productizationEventService : null, applicationEventService).call();
                    });
            MDC.remove("traceId");
        });
    }

    public void coreTableSync(Map<String, Object> data, String schema, String table, boolean pushEvent) {
        coreTableSync(SourceEntity.builder()
                .data(data)
                .before(new HashMap<>(0))
                .name(table)
                .schema(schema)
                .opType("UPDATE")
                .build(), pushEvent);
    }

    public void coreTableSync(List<Map<String, Object>> data, String schema, String table, boolean pushEvent) {
        coreTableSync(data.stream().map(s -> SourceEntity.builder()
                .schema(schema)
                .name(table)
                .data(s)
                .before(new HashMap<>(0))
                .opType("UPDATE")
                .build()).collect(Collectors.toList()), pushEvent);
    }

    public void coreTableSync(SourceEntity sourceEntity) {
        log.info("pre sync business entity: {}", sourceEntity);
        coreTableSync(sourceEntity, true);
    }

    /**
     * 获取配置
     *
     * @param schema 库名
     * @param table  表名
     * @deprecated 已经实例化为converter, 不需要再读取配置
     */
    public @Nullable
    Map<String, Object> getConfig(@NonNull String schema, @NonNull String table) {
        return configs.computeIfAbsent(schema + "-" + table, key -> {
            List<Map> result = mongoTemplate.find(new Query(Criteria.where("_id").is(key)), Map.class, "sync_config");
            if (result.isEmpty()) {
                log.warn("[{}] could not find sync config!", key);
                return null;
            }
            // 使用的_id查询, 只可能有一个结果
            //noinspection unchecked
            return (Map<String, Object>)result.get(0);
        });
    }

    private final Map<String, Map<String, CoreTableConverter>> converters = new ConcurrentHashMap<>();

    private final Map<String, Map<String, Object>> configs = new ConcurrentHashMap<>();

    // 懒加载方式获取转换器
    public @NonNull
    Map<String, CoreTableConverter> getConverter(String schema, String table) {
        String _id = CommonUtils.getMongoConfigId(schema, table);
        // 内层map作为一个整体修改, 因此不考虑并发问题
        Map<String, CoreTableConverter> converterMap = converters.computeIfAbsent(_id, key -> readConverters(schema, table));
        if (converterMap.isEmpty())
            log.warn("[{}] could not find sync config!", _id);
        return converterMap;
    }

    public int expireCache() {
        int i = converters.size();
        converters.clear();
        configs.clear();
        return i;
    }

    public int expireCache(String schemaName, String tableName) {
        String _id = CommonUtils.getMongoConfigId(schemaName, tableName);
        configs.remove(_id);
        if (converters.remove(_id) == null)
            return 0;
        return 1;
    }

    @PostConstruct
    private void init() {
        // 将mongo库中所有配置读取到内存
        reloadAllConverters();
    }

    public void reloadAllConverters() {
        expireCache();
        mongoTemplate.getCollection(SYNC_COLLECTION_NAME).find()
                .forEach((Consumer<? super Document>) document ->
                        converters.put(document.getString("schema") + "-" + document.getString("table"), readConvertFromDocument(document)));
        log.debug("load table converters cache: {}", converters);
    }

    /**
     * 从一个mongo document中读取转换器
     *
     * @param document mongo document
     * @return 当前文档配置包含的转换器
     */
    private Map<String, CoreTableConverter> readConvertFromDocument(Document document) {
        Map<String, CoreTableConverter> converterMap = new HashMap<>();

        // 抽取公共的同步配置
        Map<String, Object> extraConfig = document.entrySet().stream().filter(o -> !("_id".equals(o.getKey()) || "converter".equalsIgnoreCase(o.getKey())))
                .collect(HashMap::new, (o, v) -> o.put(v.getKey(), v.getValue()), HashMap::putAll);

        //noinspection unchecked
        document.get("converter", List.class).forEach(c -> {
            @SuppressWarnings("unchecked") Map<String, Object> convertConfig = (Map<String, Object>) c;
            String destTable = (String) convertConfig.get("dest_table");
            boolean directForward = (boolean) convertConfig.getOrDefault("direct_forward", false);

            if (directForward)
                converterMap.put(destTable, CoreTableConverter.directForwardConverter());
            else
                //noinspection unchecked
                converterMap.put(destTable, CoreTableConverter.buildFromConverterConfig(
                        (Map<String, Object>) c,
                        expressionService,
                        autoConfigurableCoreTableDao,
                        coreTableDao,
                        document.getString("schema") + "-" + document.getString("table") + "=>[" + destTable + "]",
                        extraConfig
                ));
        });
        return converterMap;
    }

    /**
     * 获取转换器
     *
     * @param schema 库名
     * @param table  表名
     */
    public Map<String, CoreTableConverter> readConverters(String schema, String table) {
        Map<String, CoreTableConverter> converterMap = new HashMap<>();
        mongoTemplate.executeQuery(new Query(Criteria.where("schema").is(schema))
                        .addCriteria(Criteria.where("table").is(table)),
                SYNC_COLLECTION_NAME, document -> converterMap.putAll(readConvertFromDocument(document)));
        return converterMap;
    }

}
