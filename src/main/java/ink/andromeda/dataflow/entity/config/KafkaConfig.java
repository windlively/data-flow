package ink.andromeda.dataflow.entity.config;

import lombok.Data;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.List;
import java.util.Map;

public class KafkaConfig {

    @Data
    public static class ConsumerConfig {

        private Map<String, Object> properties;

    }

    @Data
    public static class ContainerConfig {

        private List<String> topics;

        private ContainerProperties.AckMode ackMode;

    }
}
