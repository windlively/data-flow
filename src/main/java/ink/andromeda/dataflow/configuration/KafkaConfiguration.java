package ink.andromeda.dataflow.configuration;

import lombok.extern.slf4j.Slf4j;
import ink.andromeda.dataflow.entity.config.KafkaConfig;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.BatchMessageListener;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kafka 配置
 * 暂不使用
 */
 @Configuration
@Slf4j
public class KafkaConfiguration {

    public KafkaConfiguration(@Qualifier("realTimeDataFetchCommitCallback") OffsetCommitCallback offsetCommitCallback) {
        // this.messageListener = messageListener;
        this.offsetCommitCallback = offsetCommitCallback;
    }

    @Bean
    @ConfigurationProperties("kafka-canal.consumer")
    KafkaConfig.ConsumerConfig canalKafkaConsumerConfig(){
        return new KafkaConfig.ConsumerConfig();
    }

    @Bean
    @ConfigurationProperties("kafka-canal.container")
    KafkaConfig.ContainerConfig canalKafkaContainerConfig(){
        return new KafkaConfig.ContainerConfig();
    }

    @Bean
    ConsumerFactory<Long, Object> consumerFactory(){
        Map<String, Object> consumerProperties = new HashMap<>();
        KafkaConfig.ConsumerConfig config = canalKafkaConsumerConfig();
        config.getProperties().forEach((k,v) -> consumerProperties.put(k.replace('-','.'), v));
        log.info("canal kafka consume config: {}", consumerProperties);
        return new DefaultKafkaConsumerFactory<>(consumerProperties);
    }

    private BatchMessageListener<Long, String> messageListener;

    private final OffsetCommitCallback offsetCommitCallback;

    @Bean
    ContainerProperties containerProperties(){
        List<String> topics = canalKafkaContainerConfig().getTopics();
        ContainerProperties containerProperties = new ContainerProperties(topics.toArray(new String[0]));
        containerProperties.setAckMode(canalKafkaContainerConfig().getAckMode());
        containerProperties.setMessageListener(messageListener);
        containerProperties.setCommitCallback(offsetCommitCallback);
        return containerProperties;
    }

    @Bean
    KafkaMessageListenerContainer<Long,Object> messageListenerContainer(){
        return new KafkaMessageListenerContainer<>(consumerFactory(), containerProperties());
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<Long,Object> kafkaListenerContainerFactory(){
        ConcurrentKafkaListenerContainerFactory<Long,Object> containerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        containerFactory.setConsumerFactory(consumerFactory());
        return containerFactory;
    }

}
