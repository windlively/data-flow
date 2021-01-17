package ink.windlively.dataflow.core.mq;

import ink.windlively.dataflow.util.GeneralTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;
import java.util.concurrent.Future;

@Slf4j
public class KafkaInstance implements MessageQueueInstance<KafkaProducer<Object, Object>, KafkaConsumer<Object, Object>> {

    private final KafkaProducer<Object, Object> kafkaProducer;

    private final String name;

    public KafkaInstance(Properties properties, String name) {
        kafkaProducer = new KafkaProducer<>(properties);
        log.info("create kafka producer: {}, properties: {}", name, GeneralTools.toJSONString(properties));
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return "kafka";
    }

    @Override
    public KafkaProducer<Object, Object> getProducer() {
        return kafkaProducer;
    }

    public Future<RecordMetadata> send(String topic, Object message) {
        Future<RecordMetadata> send = kafkaProducer.send(new ProducerRecord<>(topic, message));
        kafkaProducer.flush();
        return send;
    }


    public Future<RecordMetadata> send(String topic, Integer partition, String key, Object message) {
        Future<RecordMetadata> send = kafkaProducer.send(new ProducerRecord<>(topic, partition, key, message));
        kafkaProducer.flush();
        return send;
    }

    public Future<RecordMetadata> send(ProducerRecord<Object, Object> producerRecord) {
        Future<RecordMetadata> send = kafkaProducer.send(producerRecord);
        kafkaProducer.flush();
        return send;
    }

    @Override
    public void close() {
        kafkaProducer.close();
    }
}
