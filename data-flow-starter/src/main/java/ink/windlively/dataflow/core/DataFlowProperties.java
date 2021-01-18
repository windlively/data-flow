package ink.windlively.dataflow.core;

import ink.windlively.dataflow.core.mq.MqInstanceConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties("data-flow")
@Data
public class DataFlowProperties {

    public static final String REDIS_KEY_PREFIX = "data-flow:";

    public static final String REDIS_INSTANCE_REGISTER_KEY = DataFlowProperties.REDIS_KEY_PREFIX + "monitor:instance";

    private String defaultMqType = "kafka";

    private boolean enable;

    @NestedConfigurationProperty
    private MqInstanceConfig[] mqInstances;

    @NestedConfigurationProperty
    private HttpOptions httpOptions = new HttpOptions();

    @Data
    public static class HttpOptions{

        private boolean enableFlowConfig = true;

        private boolean enableMonitor = true;

    }

}
