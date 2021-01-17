package ink.windlively.dataflow.util.kafka.serialize;

import ink.windlively.dataflow.core.SourceEntity;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;

import static ink.windlively.dataflow.util.GeneralTools.GSON;

public class SourceEntitySerializer implements Serializer<SourceEntity> {

    @Override
    public byte[] serialize(String topic, SourceEntity data) {
        return GSON().toJson(data).getBytes(StandardCharsets.UTF_8);
    }

}
