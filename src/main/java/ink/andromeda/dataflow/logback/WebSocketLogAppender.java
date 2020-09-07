package ink.andromeda.dataflow.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import ink.andromeda.dataflow.ws.AppLogPushServer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.joda.time.DateTime;
import org.slf4j.MDC;

import java.net.InetAddress;

@Setter
@Getter
public class WebSocketLogAppender extends AppenderBase<ILoggingEvent> {

    private String topic;
    private String brokerList;
    private String department;
    public static String environment = "DEFAULT";
    private String type;
    private KafkaProducer<String, String> producer;
    private String hostName;
    private String ip;

    public WebSocketLogAppender() {
    }

    protected void append(ILoggingEvent eventObject) {
        String level = eventObject.getLevel().toString();
        if (level.equals("DEBUG") || level.equals("TRACE")) {
            return;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("department", this.department);
        jsonObject.put("type", this.type);
        jsonObject.put("created", (new DateTime(eventObject.getTimeStamp())).toString("yyyy-MM-dd HH:mm:ss.SSS"));
        jsonObject.put("traceId", MDC.get("traceId"));
        jsonObject.put("level", level);
        jsonObject.put("message", eventObject.getFormattedMessage());
        jsonObject.put("hostName", this.hostName);
        jsonObject.put("ip", this.ip);
        jsonObject.put("thread", eventObject.getThreadName());
        jsonObject.put("loggerName", eventObject.getLoggerName());
        jsonObject.put("throwableContext", WeLogUtils.throwableToString(eventObject.getThrowableProxy()));
        AppLogPushServer.pushLog(jsonObject.toJSONString());
        // LogPushService.pushLog(jsonObject.toJSONString());
    }

    public void start() {
        super.start();
        try {
            InetAddress addr = InetAddress.getLocalHost();
            this.hostName = addr.getHostName();
            this.ip = addr.getHostAddress();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
