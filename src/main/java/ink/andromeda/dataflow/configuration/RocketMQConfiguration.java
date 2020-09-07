package ink.andromeda.dataflow.configuration;

import com.aliyun.openservices.ons.api.*;
import com.aliyun.openservices.ons.api.order.OrderProducer;
import com.google.common.base.Joiner;
import ink.andromeda.dataflow.RocketMessageListener;
import lombok.extern.slf4j.Slf4j;
import net.abakus.coresystem.entity.BatchSyncTaskInfo;
import net.abakus.coresystem.entity.config.RocketConfig;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static net.abakus.coresystem.util.CommonUtils.setBeanProperties;

/**
 * rocket MQ 配置
 * 暂未使用
 */
@Configuration
@Slf4j
public class RocketMQConfiguration {

    public final static String TOPIC_NAME = "abakus-coresystem-prod";

    public final static String HIS_SYNC_BATCH_TAG = "history-sync-batch";

    private final RocketMessageListener messageListener;

    public RocketMQConfiguration(RocketMessageListener messageListener) {
        this.messageListener = messageListener;
    }

    @Bean
    @ConfigurationProperties("rocket-mq")
    RocketConfig rocketConfig() {
        return new RocketConfig();
    }

    // @Bean
    DefaultMQProducer rocketProducer() throws MQClientException {
        DefaultMQProducer defaultMQProducer = new DefaultMQProducer();
        setBeanProperties(rocketConfig().getProducer(), defaultMQProducer, '-');
        defaultMQProducer.start();
        return defaultMQProducer;
    }

    // @Bean
    DefaultMQPushConsumer rocketConsumer() throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer();
        Map<String, Object> consumerConfig = rocketConfig().getConsumer();
        setBeanProperties(rocketConfig().getConsumer(), consumer, '-');
        @SuppressWarnings("unchecked") Map<Integer, Map<String, Object>> topics = (Map<Integer, Map<String, Object>>) consumerConfig.get("topics");
        topics.values().forEach(topic -> {
            String topicName = (String) topic.get("topic");
            @SuppressWarnings("unchecked") Map<Object, String> tags = (Map<Object, String>) topic.get("tags");
            String tag = Joiner.on("||").join(tags.values());
            try {
                consumer.subscribe(topicName, tag);
            } catch (MQClientException e) {
                log.error(e.getErrorMessage(), e);
                e.printStackTrace();
            }
        });
        consumer.registerMessageListener(messageListener);
        consumer.start();
        return consumer;
    }

    @Bean
    Producer onsRocketProducer() {

        Properties properties = getONSFactoryProperties("producer");
        // properties.setProperty(PropertyKeyConst.GROUP_ID, "GID_core_system_event_prod");
        // AccessKey 阿里云身份验证，在阿里云服务器管理控制台创建
        // properties.put(PropertyKeyConst.AccessKey, "LTAI4G5Xpp2GfzFQZR2tCcSH");
        // SecretKey 阿里云身份验证，在阿里云服务器管理控制台创建
        // properties.put(PropertyKeyConst.SecretKey, "xxB8F2JMIUg6pJuSslCfJB4kmfqokl");
        //设置发送超时时间，单位毫秒
        // properties.setProperty(PropertyKeyConst.SendMsgTimeoutMillis, "3000");
        // 设置 TCP 接入域名，到控制台的实例基本信息中查看
        // properties.put(PropertyKeyConst.NAMESRV_ADDR,
        // "http://MQ_INST_1346094110230574_BXNoHaKU.cn-qingdao.mq-internal.aliyuncs.com:8080");

        Producer producer = ONSFactory.createProducer(properties);
        producer.start();
        return producer;
    }

    @Bean
    OrderProducer onsRocketOrderProducer() {

        Properties properties = getONSFactoryProperties("producer");
        OrderProducer producer = ONSFactory.createOrderProducer(properties);
        producer.start();
        return producer;
    }

    @Bean
    Consumer onsRocketConsumer() {
        return ONSFactory.createConsumer(getONSFactoryProperties("consumer"));
    }

    private Properties getONSFactoryProperties(String type) {
        //noinspection unchecked
        return ((Map<String, String>) Optional
                .ofNullable(
                        Optional.ofNullable(rocketConfig().getOnsFactory())
                                .orElseThrow(() -> new IllegalArgumentException("ons factory config is null"))
                                .get(type)
                ).orElseThrow(() -> new IllegalArgumentException("ons producer factory config is null")))
                .entrySet().stream().collect(Properties::new, (p, e) ->
                        p.put(e.getKey(), e.getValue()), Properties::putAll);
    }

}
