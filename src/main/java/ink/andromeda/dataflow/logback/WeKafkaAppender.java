package ink.andromeda.dataflow.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import lombok.Data;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.slf4j.MDC;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.Properties;


@Data
public class WeKafkaAppender extends AppenderBase<ILoggingEvent> {

    public static final String DESTINATION_ADDRESS_URL = "http://169.254.169.254/latest/meta-data/local-ipv4";
    public static final String DESTINATION_HOSTNAME_URL = "http://169.254.169.254/latest/meta-data/hostname";
    private String topic;
    private String brokerList;
    private String department;
    private String environment;
    private String type;
    private KafkaProducer<String, String> producer;
    private String destinationAddress;
    private String destinationHostname;

    public WeKafkaAppender() {
    }


    @Override
    protected void append(ILoggingEvent eventObject) {
        String level = eventObject.getLevel().toString();
        if (!level.equals("DEBUG") && !level.equals("TRACE")) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("department", this.department);
            jsonObject.put("type", this.type);
            jsonObject.put("created", (new DateTime()).toString("yyyy-MM-dd HH:mm:ss"));
            jsonObject.put("traceId", MDC.get("traceId"));
            jsonObject.put("level", eventObject.getLevel().toString());
            jsonObject.put("environment", this.environment);
            jsonObject.put("message", eventObject.getFormattedMessage());
            jsonObject.put("logtype", "java");
            jsonObject.put("throwableContext", WeLogUtils.throwableToString(eventObject.getThrowableProxy()));
            ProducerRecord<String, String> data = new ProducerRecord(this.topic, jsonObject.toJSONString());
            this.producer.send(data);
        }

        if (Objects.equals(eventObject.getLoggerName(), "net.wecash.coresystem.backoffice.filter.TraceIdFilter")) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("dateTime", LocalDateTime.now(DateTimeZone.getDefault()).toString("dd/MM/yyyy HH:mm:ss"));
            jsonObject.put("applicationName", "financialSystem");
            jsonObject.put("eventType", MDC.get("eventType"));
            jsonObject.put("sourceAddress", MDC.get("xForwardFor"));
            jsonObject.put("sourceHostname", "");
            jsonObject.put("sourceUserid", MDC.get("sourceUserid"));
            jsonObject.put("sourceObject", "");
            jsonObject.put("destinationAddress", Strings.nullToEmpty(this.destinationAddress));
            jsonObject.put("destinationHostname", Strings.nullToEmpty(this.destinationHostname));
            jsonObject.put("destinationUserid", "");
            jsonObject.put("destinationObject", "");
            jsonObject.put("result", MDC.get("result"));
            jsonObject.put("message", MDC.get("message"));
            jsonObject.put("env", getEnvironment());
            jsonObject.put("method", eventObject.getLoggerName());
            jsonObject.put("traceId", MDC.get("traceId"));
            jsonObject.put("logtype", "audit-trail");
            jsonObject.put("department", this.department);
            jsonObject.put("path", MDC.get("path"));
            jsonObject.put("requestMethod", MDC.get("requestMethod"));
            ProducerRecord<String, String> data = new ProducerRecord(this.topic, jsonObject.toJSONString());
            this.producer.send(data);
        }
    }


    @Override
    public void start() {
        super.start();
        if (!Strings.isNullOrEmpty(this.topic) && !Strings.isNullOrEmpty(this.brokerList) && !Strings.isNullOrEmpty(this.department) && !Strings.isNullOrEmpty(this.environment) && !Strings.isNullOrEmpty(this.type)) {
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
        System.out.println("开始获取地址...");
        try {
            RestTemplate restTemplate = new RestTemplate();
            this.destinationAddress = restTemplate.getForObject(DESTINATION_ADDRESS_URL, String.class);
            System.out.println("destinationAddress success: " + this.destinationAddress);
        } catch (RestClientException e) {
            e.printStackTrace();
            System.out.println("destinationAddress fail: " + e.getMessage());
        }
        try {
            RestTemplate restTemplate = new RestTemplate();
            this.destinationHostname = restTemplate.getForObject(DESTINATION_HOSTNAME_URL, String.class);
            System.out.println("destinationHostname success: " + this.destinationHostname);
        } catch (RestClientException e) {
            e.printStackTrace();
            System.out.println("destinationHostname fail: " + e.getMessage());
        }
        System.out.println("获取地址完毕...");
    }


    @Override
    public void stop() {
        super.stop();
        if (this.producer != null) {
            this.producer.close();
        }
    }


}
