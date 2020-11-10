package ink.andromeda.dataflow.server.canal;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.CanalPacket;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.protocol.exception.CanalClientException;
import com.google.protobuf.ByteString;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class ByteArrayCanalMessageConverter implements Converter<byte[], Message> {

    @Nullable
    @Override
    public Message convert(@NonNull byte[] source) {
        try {
            CanalPacket.Packet p = CanalPacket.Packet.parseFrom(source);
            switch (p.getType()) {
                case MESSAGES: {
                    if (!p.getCompression().equals(CanalPacket.Compression.COMPRESSIONCOMPATIBLEPROTO2)) {
                        throw new CanalClientException("compression is not supported in this connector");
                    }
                    CanalPacket.Messages messages = CanalPacket.Messages.parseFrom(p.getBody());
                    Message result = new Message(messages.getBatchId());
                    for (ByteString byteString : messages.getMessagesList()) {
                        CanalEntry.Entry entry = CanalEntry.Entry.parseFrom(byteString);
                        if (CanalEntry.EntryType.ROWDATA.equals(entry.getEntryType())) {
                            result.addEntry(entry);
                        }
                    }
                    return result;
                }
                case ACK: {
                    CanalPacket.Ack ack = CanalPacket.Ack.parseFrom(p.getBody());
                    throw new CanalClientException("something goes wrong with reason: " + ack.getErrorMessage());
                }
                default: {
                    throw new CanalClientException("unexpected packet type: " + p.getType());
                }
            }
        } catch (Exception e) {
            throw new CanalClientException("deserializer failed", e);
        }
    }
}
