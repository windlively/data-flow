package ink.andromeda.dataflow.util.kafka.serialize;

import ink.andromeda.dataflow.core.SourceEntity;
import ink.andromeda.dataflow.util.converter.JSONStringToSourceEntityConverter;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;

import java.nio.charset.StandardCharsets;

public class SourceEntityDeserializer extends ErrorHandlingDeserializer<SourceEntity> {

    private final static JSONStringToSourceEntityConverter converter = new JSONStringToSourceEntityConverter();

    public SourceEntityDeserializer() {
        super((topic, data) -> converter.convert(new String(data, StandardCharsets.UTF_8)));
    }

}
