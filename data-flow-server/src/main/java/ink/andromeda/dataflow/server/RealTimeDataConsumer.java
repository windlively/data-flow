package ink.andromeda.dataflow.server;

import com.alibaba.otter.canal.protocol.Message;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ink.andromeda.dataflow.core.DataRouter;
import ink.andromeda.dataflow.core.SourceEntity;
import ink.andromeda.dataflow.server.canal.ByteArrayCanalMessageConverter;
import ink.andromeda.dataflow.server.canal.CanalMessageToSourceEntityConverter;
import ink.andromeda.dataflow.server.entity.OGGMessage;
import ink.andromeda.dataflow.server.entity.DefaultServerConfig;
import ink.andromeda.dataflow.util.converter.JSONStringToOGGMessageConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.springframework.kafka.listener.BatchAcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static ink.andromeda.dataflow.server.entity.DefaultServerConfig.KafkaMsgType.CANAL;


@Slf4j
public class RealTimeDataConsumer implements BatchAcknowledgingMessageListener<Long, byte[]> {

    private final DefaultServerConfig defaultServerConfig;

    private final DataRouter dataRouter;

    private Map<String, List<String>> listenTable;

    public RealTimeDataConsumer(DefaultServerConfig defaultServerConfig, DataRouter dataRouter) {
        this.defaultServerConfig = defaultServerConfig;
        this.dataRouter = dataRouter;
    }

    @PostConstruct
    public void init() {
        //noinspection unchecked
        listenTable =
                defaultServerConfig
                        .getListenTableConfig()
                        .stream()
                        .collect(Collectors.toMap(m -> (String) m.get("schema"), m -> new ArrayList<>(((Map<Object, String>) m.get("tables")).values())));
    }

    @Override
    public void onMessage(List<ConsumerRecord<Long, byte[]>> list, Acknowledgment acknowledgment) {
        try {
            log.debug("kfk收到消息。Size = {}", list.size());
            for (ConsumerRecord<Long, byte[]> record : list) {

                List<SourceEntity> sourceEntities = convert(record.value());
                sourceEntities.forEach(sourceEntity -> {
                    // 正则表达式匹配库名
                    List<String> listenTableNames = listenTable.entrySet()
                            .stream()
                            .filter(e -> sourceEntity.getSchema().matches(e.getKey()))
                            .map(Map.Entry::getValue)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());

                    String schemaName = sourceEntity.getSchema();
                    String tableName = sourceEntity.getName();

                    if (listenTableNames.isEmpty()) {
                        log.debug("business entity: {} not in schema {}", sourceEntity, listenTable.keySet().toString());
                        return;
                    }

                    if (listenTableNames.stream().noneMatch(tableName::matches)) {
                        log.debug("business entity: {} not match schema {} and regs {}", sourceEntity, schemaName, listenTable.get(schemaName).toString());
                        return;
                    }

                    try {
                        dataRouter.routeAndProcess(sourceEntity);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }

                });
            }
            acknowledgment.acknowledge();
        } catch (Throwable ex) {
            log.error(ex.toString(), ex);
            ex.printStackTrace();
        }

    }

    private final ByteArrayCanalMessageConverter byteArrayCanalMessageConverter = new ByteArrayCanalMessageConverter();

    private final CanalMessageToSourceEntityConverter canalMessageToSourceEntityConverter = new CanalMessageToSourceEntityConverter();

    private final JSONStringToOGGMessageConverter jsonStringToOGGMessageConverter = new JSONStringToOGGMessageConverter();

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES)
            .create();

    public List<SourceEntity> convert(byte[] body) {
        DefaultServerConfig.KafkaMsgType kafkaMsgType = defaultServerConfig.getKafkaMsgType();
        switch (kafkaMsgType) {
            case CANAL:
                Message convert = byteArrayCanalMessageConverter.convert(body);
                Assert.notNull(convert, "convert canal message is null");
                return canalMessageToSourceEntityConverter.convert(convert);
            case CANAL_PLAIN:


            case OGG:
                OGGMessage message = jsonStringToOGGMessageConverter.convert(new String(body, StandardCharsets.UTF_8));
                Assert.notNull(message, "convert ogg message is null");
                String schemaName = message.getSchemaName();
                String tableName = message.getSimpleTableName();
                SourceEntity sourceEntity = SourceEntity.builder()
                        .before(message.getBefore())
                        .data(message.getAfter())
                        .opType(convertOpType(message.getOpType()))
                        .schema(schemaName)
                        .name(tableName)
                        .source("")
                        .build();
                return Collections.singletonList(sourceEntity);
            case SOURCE_ENTITY:
                return Collections.singletonList(gson.fromJson(new String(body, StandardCharsets.UTF_8), SourceEntity.class));
            default:
                throw new IllegalArgumentException("unknown kafka msg type: " + kafkaMsgType);
        }
    }

    private static String convertOpType(String oggOpType) {
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
