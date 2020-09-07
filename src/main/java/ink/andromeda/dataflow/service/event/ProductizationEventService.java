package ink.andromeda.dataflow.service.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.*;
import com.aliyun.openservices.ons.api.order.OrderProducer;
import ink.andromeda.dataflow.service.ExpressionService;
import lombok.extern.slf4j.Slf4j;
import ink.andromeda.dataflow.datasource.dao.CommonDao;
import ink.andromeda.dataflow.entity.CanalType;
import ink.andromeda.dataflow.entity.SourceEntity;
import ink.andromeda.dataflow.entity.CoreEntity;
import net.abakus.coresystem.entity.po.ProdLinkRelationConf;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static ink.andromeda.dataflow.configuration.RocketMQConfiguration.TOPIC_NAME;
import static ink.andromeda.dataflow.service.MongoConfigService.EVENT_COLLECTION_NAME;
import static net.abakus.coresystem.util.CommonUtils.*;


@Slf4j
@Service
public class ProductizationEventService implements EventService {

    private final ExpressionService expressionService;

    private final MongoTemplate mongoTemplate;

    private final Producer mqProducer;

    private final OrderProducer orderProducer;

    private final ProductizationConfigService productizationConfigService;

    private final CommonDao commonDao;

    public ProductizationEventService(ExpressionService expressionService,
                                      MongoTemplate mongoTemplate,
                                      Producer mqProducer,
                                      OrderProducer orderProducer,
                                      ProductizationConfigService productizationConfigService,
                                      CommonDao commonDao) {
        this.expressionService = expressionService;
        this.mongoTemplate = mongoTemplate;
        this.mqProducer = mqProducer;
        this.orderProducer = orderProducer;
        this.productizationConfigService = productizationConfigService;
        this.commonDao = commonDao;
    }

    @PostConstruct
    public void init() {

    }

    public List<EventMessage> inferEvent(SourceEntity sourceEntity, CoreEntity coreEntity) {
        List<EventMessage> eventMessageResults = new ArrayList<>();
        String schemaName = sourceEntity.getSchema();
        String tableName = sourceEntity.getTable();
        JSONObject configs = getConfig(schemaName, tableName);
        if (configs == null) {
            eventMessageResults.add(EventMessage.builder()
                    .msg(String.format("schema: %s, table: %s, event config is null", schemaName, tableName)).build());
            return eventMessageResults;
        }
        // boolean multiMatch = configs.getBooleanValue("multi_match");
        List<JSONObject> eventList = configs.getJSONArray("event_list").toJavaList(JSONObject.class);
        for (JSONObject config : eventList) {
            String eventName = config.getString("event_name");
            EventMessage eventMessage = EventMessage.builder()
                    .eventSourceSchema(sourceEntity.getSchema())
                    .eventSourceTable(sourceEntity.getTable())
                    .eventName(eventName)
                    .description(eventName)
                    .eventSourceType(sourceEntity.getOpType())
                    .build();
            eventMessageResults.add(eventMessage);
            if (matchEvent(sourceEntity, coreEntity, config)) {
                log.info("match event, event name: {}", config.getString("event_name"));
                eventMessage.setMsg("success");
                eventMessage.setSuccess(true);
                boolean delay = config.getBooleanValue("delay");
                String eventDataConfig = config.getString("event_data");
                if (!StringUtils.isEmpty(eventDataConfig)) {
                    Object eventData = expressionService.executeExpression(eventDataConfig, expressionService.evaluationContext());
                    eventMessage.setData((JSONObject) JSONObject.toJSON(eventData));
                }else {
                    //noinspection unchecked
                    Map<String, String> outputFields = (Map<String, String>) config.get("out_fields");
                    if (outputFields == null || outputFields.isEmpty()) {
                        outputFields = sourceEntity.getData().entrySet().stream()
                                .collect(Collectors.toMap(e -> schemaName + "." + tableName + "." + e.getKey(),
                                        Map.Entry::getKey));
                    }
                    @SuppressWarnings("unchecked") Map<String, Object> rootData =
                            ((Map<String, Object>) expressionService.evaluationContext().getRootObject().getValue());
                    Map<String, Object> outData = new HashMap<>(outputFields.size());
                    outputFields.forEach((k, v) -> {
                        String[] split = k.split("\\.");
                        if (split.length != 3)
                            throw new IllegalStateException("wrong output field name format: " + k);
                        assert rootData != null;
                        //noinspection unchecked
                        Object value = ((Map<String, Map<String, Object>>) rootData.getOrDefault(split[0], Collections.emptyMap()))
                                .getOrDefault(split[1], Collections.emptyMap()).get(split[2]);
                        outData.put(v, value);
                    });
                    eventMessage.setData(outData);
                    JSONObject customOutFields;
                    if ((customOutFields = config.getJSONObject("custom_out_fields")) != null) {
                        customOutFields.forEach((k, v) ->
                                outData.put(k, expressionService.executeExpression((String) v, expressionService.evaluationContext())));
                    }
                }


                MessageProperties messageProperties = new MessageProperties();
                messageProperties.setContentType(MessageProperties.DEFAULT_CONTENT_TYPE);

                String msg = toJSONString(eventMessage);
                Message message = new Message();
                message.setBody(msg.getBytes(StandardCharsets.UTF_8));
                message.setKey(MDC.get("traceId"));
                message.setTopic(TOPIC_NAME);
                message.setTag(config.getString("event_name"));

                SendResult sendResult;

                if (delay) {
                    // rabbitTemplate.send("cs-event", message);
                }

                // 贷后增加改为顺序消息
                // todo 顺序消息改成依赖配置
                if (config.getString("event_name").equalsIgnoreCase("core-loan-tail")) {
                    sendResult = orderProducer.send(message, sourceEntity.getData().getString("LOAN_NO"));
                } else {
                    sendResult = mqProducer.send(message);
                }

                String postProcessor = config.getString("post_processor");

                if (StringUtils.isNotEmpty(postProcessor)) {
                    expressionService.executeExpression(postProcessor, expressionService.evaluationContext());
                }
                log.info("send event message success, {}, message: {}", sendResult.toString(), msg);
                // if (!multiMatch) break;
            } else {
                eventMessage.setMsg(String.format("not match event %s of condition: %s", eventName, joinCondition(sourceEntity.getOpType(), config)));
            }
        }
        return eventMessageResults;
    }

    private final static String LINK_TABLE_DATA_SQL = "SELECT * FROM %s.%s WHERE %s = %s";

    private boolean matchEvent(SourceEntity sourceEntity, CoreEntity coreEntity, JSONObject config) {
        StandardEvaluationContext evaluationContext = expressionService.evaluationContext();
        Map<String, Object> rootObject = new HashMap<>();
        evaluationContext.setRootObject(rootObject);

        String currentSchema = sourceEntity.getSchema();
        String currentTable = sourceEntity.getTable();

        Map<String, Map<String, Object>> currentSchemaData = new HashMap<>();
        Map<String, Object> currentTableData = sourceEntity.getData();
        currentSchemaData.put(currentTable, currentTableData);
        rootObject.put(currentSchema, currentSchemaData);
        rootObject.put("CE", coreEntity.getEntity());
        JSONArray linkTables;
        if ((linkTables = config.getJSONArray("link_tables")) != null && !linkTables.isEmpty()) {
            linkTables.forEach(t -> {
                if (t instanceof String) {
                    String[] split = ((String) t).split("\\.");
                    if (split.length != 2)
                        throw new IllegalStateException("wrong link table name format: " + t);
                    String schema = split[0];
                    String table = split[1];
                    ProdLinkRelationConf query = ProdLinkRelationConf
                            .builder()
                            .leftSchema(currentSchema)
                            .leftTable(currentTable)
                            .rightSchema(schema)
                            .rightTable(table)
                            .build();
                    ProdLinkRelationConf linkRelation = Optional.ofNullable(
                            productizationConfigService.getProdLinkRelationCondition(query))
                            .orElse(productizationConfigService.getProdLinkRelationCondition(query.swapPosition()));
                    if (linkRelation == null)
                        throw new IllegalStateException(
                                String.format("table '%s.%s' and '%s' link relation not found",
                                        currentSchema, currentTable, t
                                ));
                    String currentTableLinkField;
                    String anotherTableLinkField;
                    if (Objects.equals(currentSchema, linkRelation.getLeftSchema())
                        && Objects.equals(currentTable, linkRelation.getLeftTable())) {
                        currentTableLinkField = linkRelation.getLeftField();
                        anotherTableLinkField = linkRelation.getRightField();
                    } else {
                        currentTableLinkField = linkRelation.getRightField();
                        anotherTableLinkField = linkRelation.getLeftField();
                    }

                    String sql = String.format(LINK_TABLE_DATA_SQL, schema, table,
                            anotherTableLinkField, javaValToSqlVal(sourceEntity.getData().get(currentTableLinkField)));
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) commonDao.select(sql, schema, "map");
                    //noinspection unchecked
                    ((Map<String, Object>) rootObject.computeIfAbsent(schema, k -> new HashMap<>())).put(table, result);
                }
            });
        }


        evaluationContext.setVariable("CANAL_TYPE", sourceEntity.getOpType());
        Object evalContextConfig = config.get("eval_context");
        if (evalContextConfig != null) {
            if (evalContextConfig instanceof List) {
                //noinspection unchecked
                ((List<Map<String, Object>>) evalContextConfig).forEach(item -> {
                    //noinspection unchecked
                    List<String> onOpTypes = (List<String>) item.get("on_op_types");
                    if (onOpTypes != null && !onOpTypes.isEmpty()
                        && onOpTypes.stream().noneMatch(t -> sourceEntity.getOpType().equalsIgnoreCase(t.trim())))
                        return;
                    String name = (String) item.get("name");
                    if (rootObject.containsKey(name)) {
                        throw new IllegalStateException("variable '" + name + "' has existed");
                    }
                    rootObject.put(name, expressionService.genEvalContextVal((JSONObject) JSON.toJSON(item), evaluationContext));
                });
            }
        }

        String matchCondition = joinCondition(sourceEntity.getOpType(), config);
        boolean match = Optional.ofNullable(expressionService.executeExpression(matchCondition, evaluationContext, boolean.class))
                .orElseGet(() -> {
                    log.warn("表达式计算异常, 结果为null, condition: {}, business entity: {}", matchCondition, sourceEntity);
                    return false;
                });
        String eventName = config.getString("event_name");
        if (!match)
            log.info("事件[{}]匹配失败, condition: {}, evaluation root object: {}", eventName, matchCondition, toJSONString(evaluationContext.getRootObject().getValue()));
        return match;
    }

    private static String joinCondition(String canalType, JSONObject config) {
        String matchCondition = config.getString("match_condition");

        String onUpdate = config.getString("on_update");
        String onInsert = config.getString("on_insert");
        String onManual = config.getString("on_manual");

        if (StringUtils.isNotEmpty(onUpdate) && CanalType.isUpdate(canalType))
            matchCondition = String.format("(%s) AND (%s)", matchCondition, onUpdate);

        if (StringUtils.isNotEmpty(onInsert) && CanalType.isUpdate(canalType))
            matchCondition = String.format("(%s) AND (%s)", matchCondition, onInsert);

        if (StringUtils.isNoneEmpty(onManual) && CanalType.isManual(canalType)) {
            matchCondition = String.format("(%s) AND (%s)", matchCondition, onManual);
        }
        return matchCondition;
    }


    private JSONObject getConfig(@NonNull String schema, @NonNull String table) {
        return eventConfigs.computeIfAbsent(schema + "-" + table, k -> {
            JSONObject result;
            if ((result = mongoTemplate
                    .findOne(new Query(Criteria.where("schema").is(schema))
                                    .addCriteria(Criteria.where("table").is(table))
                            , JSONObject.class, EVENT_COLLECTION_NAME)) == null)
                log.warn("[{}] could not find event config!", k);
            return result;
        });
    }

    private final Map<String, JSONObject> eventConfigs = new ConcurrentHashMap<>();

    public int expireCache() {
        int i = eventConfigs.size();
        eventConfigs.clear();
        return i;
    }
}
