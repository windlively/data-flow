package ink.windlively.dataflow.core.mq;

import lombok.Data;

import java.util.Properties;

@Data
public class MqInstanceConfig {

    private String name;

    private String type;

    private Properties properties;

}
