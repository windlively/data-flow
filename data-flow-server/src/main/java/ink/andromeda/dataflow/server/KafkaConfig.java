package ink.andromeda.dataflow.server;

import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.List;
import java.util.Map;

@Data
public class KafkaConfig {

    @NestedConfigurationProperty
    private ConsumerConfig consumer;

    @NestedConfigurationProperty
    private ContainerConfig container;

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
