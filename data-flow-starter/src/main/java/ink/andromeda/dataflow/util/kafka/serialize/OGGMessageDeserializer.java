package ink.andromeda.dataflow.util.kafka.serialize;

import ink.andromeda.dataflow.server.entity.OGGMessage;
import ink.andromeda.dataflow.util.converter.JSONStringToOGGMessageConverter;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.StandardCharsets;

public class OGGMessageDeserializer implements Deserializer<OGGMessage> {

    private static final JSONStringToOGGMessageConverter converter = new JSONStringToOGGMessageConverter();

    public OGGMessageDeserializer(){ }

    @Override
    public OGGMessage deserialize(String topic, byte[] data) {
        return converter.convert(new String(data , StandardCharsets.UTF_8));
    }
}
