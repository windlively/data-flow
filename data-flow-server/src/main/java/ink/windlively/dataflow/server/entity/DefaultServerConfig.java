package ink.windlively.dataflow.server.entity;

import ink.windlively.dataflow.server.KafkaConfig;
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

    @NestedConfigurationProperty
    private KafkaMsg kafkaMsg = new KafkaMsg();

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
        SOURCE_ENTITY,
        /**
         * 其他格式的json
         */
        JSON,

        /**
         * 其他格式的json列表
         */
        JSON_ARRAY

    }

    @Data
    public static class JsonMsgConvertConfig{

        private String id;

        private String key;

        private String source;

        private String schema;

        private String name;

        private String timestamp;

        private String opType;

        private String data;

        private String before;

    }

    @Data
    public static class KafkaMsg{

        private KafkaMsgType type = KafkaMsgType.CANAL;

        private JsonMsgConvertConfig jsonMsgConvertConfig;

    }
}
