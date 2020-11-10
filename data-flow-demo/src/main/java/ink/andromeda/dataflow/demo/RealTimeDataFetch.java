package ink.andromeda.dataflow.demo;

import ink.andromeda.dataflow.core.DataRouter;
import ink.andromeda.dataflow.core.SourceEntity;
import ink.andromeda.dataflow.core.TransferEntity;
import ink.andromeda.dataflow.server.entity.OGGMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class RealTimeDataFetch {

    private final DataRouter dataRouter;

    public RealTimeDataFetch(DataRouter dataRouter) {
        this.dataRouter = dataRouter;
    }

    @KafkaListener(topics = "test", errorHandler = "kafkaListenerErrorHandler")
    public void onMessage(ConsumerRecord<String, OGGMessage> consumerRecord, Acknowledgment acknowledgment){
        try {
            OGGMessage oggMessage = consumerRecord.value();
            MDC.put("traceId", consumerRecord.key());
            log.info("receive ogg message: {}", oggMessage);
            String source = "test";
            List<TransferEntity> transferEntityList = dataRouter.routeAndProcess(SourceEntity.builder()
                    .source(source)
                    .name(oggMessage.getSimpleTableName())
                    .schema(oggMessage.getSchemaName())
                    .data(oggMessage.getAfter())
                    .before(oggMessage.getBefore())
                    .opType(oggMessage.getOpType())
                    .build());
            log.info("process by {} flow", transferEntityList.size());
        }catch (Exception ex){
            log.error(ex.getMessage(), ex);
        }
        acknowledgment.acknowledge();
    }


}
