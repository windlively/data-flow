package ink.andromeda.dataflow;

import ink.andromeda.dataflow.core.DataRouter;
import ink.andromeda.dataflow.core.SourceEntity;
import ink.andromeda.dataflow.entity.OGGMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

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
            String source = "test";

            dataRouter.routeAndProcess(SourceEntity.builder()
                    .source(source)
                    .name(oggMessage.getSimpleTableName())
                    .schema(oggMessage.getSchemaName())
                    .data(oggMessage.getAfter())
                    .before(oggMessage.getBefore())
                    .opType(oggMessage.getOpType())
                    .build());

            log.info(oggMessage.toString());
        }catch (Exception ex){
            log.error(ex.getMessage(), ex);
        }
        acknowledgment.acknowledge();
    }


}
