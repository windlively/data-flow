package ink.windlively.dataflow.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ink.windlively.dataflow.util.GeneralTools;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.joda.time.DateTime;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


@Data
public class KafkaLogAppender extends AppenderBase<ILoggingEvent> {

    private String topic;
    private String brokerList;
    private String department;
    private String environment;
    private String type;
    private KafkaProducer<String, String> producer;
    private String destinationAddress;
    private String destinationHostname;

    public KafkaLogAppender() {
    }


    @Override
    protected void append(ILoggingEvent eventObject) {
        String level = eventObject.getLevel().toString();
        if (!level.equals("DEBUG") && !level.equals("TRACE")) {
            Map<String, Object> jsonObject = new HashMap<>();
            jsonObject.put("department", this.department);
            jsonObject.put("type", this.type);
            jsonObject.put("created", (new DateTime()).toString("yyyy-MM-dd HH:mm:ss.SSS"));
            jsonObject.put("traceId", MDC.get("traceId"));
            jsonObject.put("level", eventObject.getLevel().toString());
            jsonObject.put("environment", this.environment);
            jsonObject.put("message", eventObject.getFormattedMessage());
            jsonObject.put("logtype", "java");
            // jsonObject.put("throwableContext", WeLogUtils.throwableToString(eventObject.getThrowableProxy()));
            ProducerRecord<String, String> data = new ProducerRecord<>(this.topic, GeneralTools.toJSONString(jsonObject));
            this.producer.send(data);
        }
    }


    @Override
    public void start() {
        super.start();
        if(StringUtils.isNoneEmpty(topic, brokerList, department, environment, type)){
            Properties properties = new Properties();
            properties.put("bootstrap.servers", this.brokerList);
            properties.put("value.serializer", StringSerializer.class.getName());
            properties.put("key.serializer", StringSerializer.class.getName());
            properties.put("acks", "0");
            properties.put("compression.type", "gzip");
            this.producer = new KafkaProducer<>(properties);
        } else {
            throw new RuntimeException("KafkaAppender必要参数缺失!");
        }
    }


    @Override
    public void stop() {
        super.stop();
        if (this.producer != null) {
            this.producer.close();
        }
    }


}
