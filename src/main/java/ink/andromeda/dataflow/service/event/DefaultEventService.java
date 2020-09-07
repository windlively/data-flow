package ink.andromeda.dataflow.service.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.SendResult;
import ink.andromeda.dataflow.service.ExpressionService;
import lombok.extern.slf4j.Slf4j;
import ink.andromeda.dataflow.entity.CanalType;
import ink.andromeda.dataflow.entity.SourceEntity;
import ink.andromeda.dataflow.entity.CoreEntity;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
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
import java.util.regex.Matcher;

import static ink.andromeda.dataflow.service.MongoConfigService.EVENT_COLLECTION_NAME;
import static net.abakus.coresystem.util.CommonUtils.*;


@Slf4j
@Service
public class DefaultEventService implements EventService{

    private final ExpressionService expressionService;

    private final MongoTemplate mongoTemplate;

    private final static String EVENT_TOPIC_NAME = "core-system";

    private final Producer mqProducer;

    public DefaultEventService(ExpressionService expressionService,
                               MongoTemplate mongoTemplate,
                               Producer mqProducer) {
        this.expressionService = expressionService;
        this.mongoTemplate = mongoTemplate;
        this.mqProducer = mqProducer;
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

        boolean multiMatch = configs.getBooleanValue("multi_match"); // 是否匹配多个事件
        List<JSONObject> eventList = configs.getJSONArray("event_list").toJavaList(JSONObject.class);
        for (JSONObject config : eventList) {
            String eventKey = config.getString("event_key");
            EventMessage eventMessage = EventMessage.builder()
                    .data(coreEntity.getEntity())
                    .eventSourceSchema(sourceEntity.getSchema())
                    .eventSourceTable(sourceEntity.getTable())
                    .eventName(config.getString("event_key"))
                    .description(config.getString("event_name"))
                    .eventSourceType(sourceEntity.getOpType())
                    .build();
            eventMessageResults.add(eventMessage);
            if (coreEntity.getName().equals(config.getString("core_table"))
                && matchEvent(sourceEntity, coreEntity, config)) {
                log.info("match event, event key: {}, event name: {}", config.getString("event_key"), config.getString("event_name"));
                eventMessage.setMsg("success");
                eventMessage.setSuccess(true);
                boolean delay = config.getBooleanValue("delay");

                String eventDataConfig = config.getString("event_data");

                if (!StringUtils.isEmpty(eventDataConfig)) {
                    Object eventData = expressionService.executeExpression(eventDataConfig, expressionService.evaluationContext());
                    eventMessage.setData((JSONObject) JSONObject.toJSON(eventData));
                }

                MessageProperties messageProperties = new MessageProperties();
                messageProperties.setContentType(MessageProperties.DEFAULT_CONTENT_TYPE);
                String msg = toJSONString(eventMessage.getData());
                Message message = new Message();
                message.setBody(msg.getBytes(StandardCharsets.UTF_8));
                message.setKey(MDC.get("traceId"));
                message.setTopic(EVENT_TOPIC_NAME);
                message.setTag(config.getString("event_key"));

                SendResult sendResult;
                if (delay) {
                    sendResult = mqProducer.send(message);
                } else {
                    sendResult = mqProducer.send(message);
                    // rabbitTemplate.send("cs-event", message);
                }
                String postProcessor = config.getString("post_processor");
                if (StringUtils.isNotEmpty(postProcessor)) {
                    expressionService.executeExpression(postProcessor, expressionService.evaluationContext());
                }
                log.info("send event message success, {}", sendResult.toString());
                if (!multiMatch) break;
            } else {
                eventMessage.setMsg(String.format("not match event %s of condition: %s", eventKey, joinCondition(sourceEntity.getOpType(), config)));
            }
        }
        return eventMessageResults;
    }

    private boolean matchEvent(SourceEntity sourceEntity, CoreEntity coreEntity, JSONObject config) {
        StandardEvaluationContext evaluationContext = expressionService.evaluationContext();
        Map<String, Object> rootObject = new HashMap<>();
        evaluationContext.setRootObject(rootObject);
        // 为了保持统一的配置风格，因此将JavaObject也转换为Map/List
        // canal bean 传入的bean
        rootObject.put("BE", sourceEntity.getData());
        // core bean 转换后的bean
        rootObject.put("CE", coreEntity.getEntity());
        rootObject.put("BEFORE", sourceEntity.getBefore());

        evaluationContext.setVariable("CANAL_TYPE", sourceEntity.getOpType());
        Object evalContextConfig = config.get("eval_context");
        if (evalContextConfig != null) {
            if (evalContextConfig instanceof Map) {
                //noinspection unchecked
                ((Map<String, Object>) evalContextConfig).forEach((name, itemConfigs) -> {
                    JSONObject val = new JSONObject();
                    JSONArray orgConfigs = (JSONArray) JSON.toJSON(itemConfigs);
                    orgConfigs.forEach(item -> val.put(((JSONObject) item).getString("name"),
                            expressionService.genEvalContextVal((JSONObject) item, evaluationContext)));
                    rootObject.put(name, val);
                });

                /*
                    // 因为逻辑相同，因此更换为上面的迭代方式

                    // 原始库的查询数据
                    JSONObject orgVal = new JSONObject();
                    JSONArray orgConfigs = ((JSONObject) evalContextConfig).getJSONArray("org");
                    orgConfigs.forEach(item -> orgVal.put(((JSONObject)item).getString("name"),
                            genEvalContextVal((JSONObject) item, evaluationContext)));
                    evaluationContext.setVariable("org", orgVal);

                    // 核心库的查询数据
                    JSONObject csVal = new JSONObject();
                    JSONArray csConfigs = ((JSONObject) evalContextConfig).getJSONArray("cs");
                    csConfigs.forEach(item -> csVal.put(((JSONObject)item).getString("name"),
                            genEvalContextVal((JSONObject) item, evaluationContext)));
                    evaluationContext.setVariable("cs", csVal);
                */

            }

            if (evalContextConfig instanceof List) {
                //noinspection unchecked
                ((List<Map<String, Object>>) evalContextConfig).forEach(item -> {
                    //noinspection unchecked
                    List<String> onOpTypes = (List<String>) item.get("on_op_types");
                    if (onOpTypes != null && !onOpTypes.isEmpty()
                        && onOpTypes.stream().noneMatch(t -> sourceEntity.getOpType().equalsIgnoreCase(t.trim())))
                        return;
                    String name = (String) item.get("name");
                    if ("BE".equals(name) || "CE".equals(name) || "BEFORE".equals(name)) {
                        log.error("数据项:{} 命名与默认数据项名称冲突，跳过该项。", name);
                        return;
                    }
                    rootObject.put(name, expressionService.genEvalContextVal((JSONObject) JSON.toJSON(item), evaluationContext));
                });
            }
        }

        String matchCondition = joinCondition(sourceEntity.getOpType(), config);
        boolean match = Optional.ofNullable(expressionService.executeExpression(matchCondition, evaluationContext, boolean.class))
                .orElseGet(() -> {
                    log.warn("表达式计算异常, 结果为null, condition: {}, business entity: {}, core entity: {}", matchCondition, sourceEntity, coreEntity);
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


    private JSONObject getConfig(@NonNull String schemaName, @NonNull String tableName) {
        return eventConfigs.computeIfAbsent(getMongoConfigId(schemaName, tableName), k -> {
            List<JSONObject> result = mongoTemplate.find(new Query(Criteria.where("_id").is(k)), JSONObject.class,
                    EVENT_COLLECTION_NAME);
            if (result.isEmpty()) {
                log.warn("[{}] could not find event config!", k);
                return null;
            }
            return result.get(0);
        });
    }

    private final Map<String, JSONObject> eventConfigs = new ConcurrentHashMap<>();

    public int expireCache() {
        int i = eventConfigs.size();
        eventConfigs.clear();
        return i;
    }


    public static void main(String[] args) {
        Matcher matcher = SQL_TABLE_NAME_REGEX.matcher("SELECT * FORM repayment_platform.repayment_schedule WHERE order_serial_num = 'C0707-7427-1918' AND df=0 AND period = 1");
        while (matcher.find())
            System.out.println(matcher.group().trim());
        System.out.println(new DateTime("2019-2-28").plusMonths(1));
    }
}
