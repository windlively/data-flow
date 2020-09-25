package ink.andromeda.dataflow.util.kafka.serialize;

import ink.andromeda.dataflow.entity.OGGMessage;
import ink.andromeda.dataflow.util.converter.JSONStringToOGGMessageConverter;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;

import java.nio.charset.StandardCharsets;

public class OGGMessageDeserializer extends ErrorHandlingDeserializer<OGGMessage> {

    private static final JSONStringToOGGMessageConverter converter = new JSONStringToOGGMessageConverter();

    public OGGMessageDeserializer(){
        super((topic, data) -> converter.convert(new String(data , StandardCharsets.UTF_8)));
    }
}
