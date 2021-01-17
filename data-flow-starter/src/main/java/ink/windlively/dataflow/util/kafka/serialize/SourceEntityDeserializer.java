package ink.windlively.dataflow.util.kafka.serialize;

import ink.windlively.dataflow.core.SourceEntity;
import ink.windlively.dataflow.util.converter.JSONStringToSourceEntityConverter;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.StandardCharsets;

public class SourceEntityDeserializer implements Deserializer<SourceEntity> {

    private final static JSONStringToSourceEntityConverter converter = new JSONStringToSourceEntityConverter();

    public SourceEntityDeserializer() {

    }

    @Override
    public SourceEntity deserialize(String topic, byte[] data) {
        return converter.convert(new String(data, StandardCharsets.UTF_8));
    }
}
