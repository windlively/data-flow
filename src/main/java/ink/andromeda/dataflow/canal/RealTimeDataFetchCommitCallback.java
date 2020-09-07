package ink.andromeda.dataflow.canal;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class RealTimeDataFetchCommitCallback  implements OffsetCommitCallback {

    @Override
    public void onComplete(Map<TopicPartition, OffsetAndMetadata> map, Exception e) {
        log.info("offset commit: {}", map, e);
    }

}
