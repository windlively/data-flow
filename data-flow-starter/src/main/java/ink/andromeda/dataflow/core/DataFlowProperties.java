package ink.andromeda.dataflow.core;

import ink.andromeda.dataflow.core.mq.MqInstanceConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties("data-flow")
@Data
public class DataFlowProperties {

    private String defaultMqType = "kafka";

    private boolean enable;

    @NestedConfigurationProperty
    private MqInstanceConfig[] mqInstances;


}
