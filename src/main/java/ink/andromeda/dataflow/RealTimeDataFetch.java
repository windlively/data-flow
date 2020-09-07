package ink.andromeda.dataflow;

import ink.andromeda.dataflow.entity.AppEventSubject;
import ink.andromeda.dataflow.entity.OGGMessage;
import ink.andromeda.dataflow.service.ApplicationEventService;
import ink.andromeda.dataflow.service.ThreadPoolService;
import ink.andromeda.dataflow.service.sync.CoreTableSyncService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ink.andromeda.dataflow.entity.SourceEntity;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.kafka.listener.BatchAcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static ink.andromeda.dataflow.util.CommonUtils.Gson;
import static ink.andromeda.dataflow.util.CommonUtils.toJSONString;

/**
 *
 */
@Component
@Slf4j
@Setter
@ConfigurationProperties("canal")
public class RealTimeDataFetch implements BatchAcknowledgingMessageListener<Long, String> {

    private final CoreTableSyncService coreTableSyncService;

    private Map<String, List<String>> listenTable;

    private final ApplicationEventService applicationEventService;

    @Setter
    private List<Map<String, Object>> listenTableConfig;

    public RealTimeDataFetch(CoreTableSyncService coreTableSyncService,
                             ApplicationEventService applicationEventService) {
        this.coreTableSyncService = coreTableSyncService;
        this.applicationEventService = applicationEventService;
    }

    @PostConstruct
    public void init() {
        //noinspection unchecked
        listenTable = listenTableConfig.stream()
                .collect(Collectors.toMap(m -> (String) m.get("schema"), m -> new ArrayList<>(((Map<Object, String>) m.get("tables")).values())));
        log.info("listenTable: {}", listenTable);
        applicationEventService.subscribe(AppEventSubject.REC_BUS_BEAN, "log", msg -> log.info("receive a business bean: {}", toJSONString(msg)));
    }

    @PreDestroy
    public void onDestroy() {
        // 等待线程池结束
        ThreadPoolService.CS_SYNC_TASK_GROUP().shutdown();
        ThreadPoolService.CS_EVENT_TASK_GROUP().shutdown();
        try {
            log.info("waiting sync and event pool shutdown...");
            ThreadPoolService.CS_SYNC_TASK_GROUP().awaitTermination(10, TimeUnit.SECONDS);
            ThreadPoolService.CS_EVENT_TASK_GROUP().awaitTermination(10, TimeUnit.SECONDS);
            log.info("sync and event pool is closed.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(List<ConsumerRecord<Long, String>> list, Acknowledgment acknowledgment) {
        // log.info("receive kafka message: {}", list.toString());
        try {
            log.info("ogg list size: {}", list.size());
            List<SourceEntity> sourceEntityList = list.stream()
                    .map(c -> {
                        try {
                            return Gson().fromJson(c.value(), OGGMessage.class);
                        } catch (Exception ex) {
                            log.error(ex.getMessage(), ex);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .sorted((o1, o2) -> (int) (o1.getPos() - o2.getPos()))
                    .map(message -> {
                        MDC.put("traceId", UUID.randomUUID().toString().replace("-", ""));
                        log.info("pos: {}", message.getPos());
                        String[] s = message.getTable().split("\\.");
                        final String schemaName = s[0].toLowerCase();
                        final String tableName = s[1].toLowerCase();
                        message.setSimpleTableName(tableName);
                        message.setSchemaName(schemaName);
                        if (StringUtils.isEmpty(schemaName) && StringUtils.isEmpty(tableName))
                            return null;
                        // 正则表达式匹配库名
                        List<String> listenTables = listenTable.entrySet()
                                .stream()
                                .filter(e -> schemaName.matches(e.getKey()))
                                .map(Map.Entry::getValue)
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList());
                        if (listenTables.isEmpty()) {
                            log.debug("ogg message: {} not in schema {}", message, listenTable.keySet().toString());
                            return null;
                        }
                        if (listenTables.stream().noneMatch(tableName::matches)) {
                            log.debug("ogg message: {} not match schema {} and regs {}", message, schemaName, listenTable.toString());
                            return null;
                        }
                        // TableEntry tableEntry = convertCanalEntry(entry);
                        log.debug("receive ogg message: {}", message);
                        // convertTableEntryToBusinessBean(tableEntry).forEach(coreTableSyncService::coreTableSync);
                        SourceEntity sourceEntity = SourceEntity.builder()
                                .before(message.getBefore())
                                .data(message.getAfter())
                                .opType(convertOpType(message.getOpType()))
                                .schema(schemaName)
                                .name(tableName)
                                .build();
                        applicationEventService.next(AppEventSubject.REC_BUS_BEAN, sourceEntity);
                        MDC.clear();
                        return sourceEntity;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            coreTableSyncService.coreTableSync(sourceEntityList, true);
            acknowledgment.acknowledge();
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex.toString(), ex);
        }
    }


    private String convertOpType(String oggOpType) {
        Objects.requireNonNull(oggOpType, "ogg OpType is null");
        switch (oggOpType) {
            case "I":
                return "INSERT";
            case "U":
                return "UPDATE";
            case "D":
                return "DELETE";
            default:
                throw new IllegalArgumentException("unknown ogg OpType: '" + oggOpType + "'");
        }
    }

}
