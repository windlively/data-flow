package ink.andromeda.dataflow.server.entity;

import ink.andromeda.dataflow.server.KafkaConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties("data-flow.server")
public class DefaultServerConfig {

    private boolean enableKafka = true;

    private boolean enableHttpInvoke = true;

    private List<Map<String, Object>> listenTableConfig;

    private String kafkaMsgType = "canal";

    @NestedConfigurationProperty
    private KafkaConfig kafka;

}
