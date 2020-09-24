package ink.andromeda.dataflow;

import ink.andromeda.dataflow.core.DataFlowManager;
import ink.andromeda.dataflow.core.DataRouter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RealTimeDataFetch {


    private final DataRouter dataRouter;

    public RealTimeDataFetch(DataFlowManager dataFlowManager,
                             DataRouter dataRouter) {
        this.dataRouter = dataRouter;
    }

    @KafkaListener(topics = "test")
    public void onMessage(ConsumerRecord<String, String> consumerRecord, Acknowledgment acknowledgment){
        String s = consumerRecord.value();

        log.info(s);
        acknowledgment.acknowledge();
    }


}
