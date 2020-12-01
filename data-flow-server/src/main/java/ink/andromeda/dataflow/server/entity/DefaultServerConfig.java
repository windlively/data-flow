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

    private KafkaMsgType kafkaMsgType = KafkaMsgType.CANAL;

    @NestedConfigurationProperty
    private KafkaConfig kafka;

    public enum  KafkaMsgType{

        /**
         * 默认的canal消息(protobuf压缩)
         */
        CANAL,

        /**
         * 解压的canal消息(JSON文本格式)
         */
        CANAL_PLAIN,

        /**
         * OGG消息(JSON文本格式)
         */
        OGG,

        /**
         * SourceEntity消息(JSON文本格式)
         */
        SOURCE_ENTITY

    }
}
