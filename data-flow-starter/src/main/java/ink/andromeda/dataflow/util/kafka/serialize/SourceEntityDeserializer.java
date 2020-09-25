package ink.andromeda.dataflow.util.kafka.serialize;

import ink.andromeda.dataflow.core.SourceEntity;
import ink.andromeda.dataflow.util.converter.JSONStringToSourceEntityConverter;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.StandardCharsets;

public class SourceEntityDeserializer implements Deserializer<SourceEntity> {

    private final JSONStringToSourceEntityConverter converter = new JSONStringToSourceEntityConverter();

    @Override
    public SourceEntity deserialize(String topic, byte[] data) {
        return converter.convert(new String(data, StandardCharsets.UTF_8));
    }
}
