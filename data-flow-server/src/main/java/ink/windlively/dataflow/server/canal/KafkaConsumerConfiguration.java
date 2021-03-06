package ink.windlively.dataflow.server.canal;

import com.alibaba.otter.canal.protocol.Message;
import ink.windlively.dataflow.core.SpringELExpressionService;
import ink.windlively.dataflow.server.KafkaConfig;
import ink.windlively.dataflow.server.RealTimeDataConsumer;
import ink.windlively.dataflow.core.DataRouter;
import ink.windlively.dataflow.server.entity.DefaultServerConfig;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.BatchMessageListener;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Canal通道的Kafka配置
 * @author baijunhan
 * @date 2020-3-27
 */
@Configuration
@ConditionalOnProperty(value = "data-flow.server.enable-kafka", havingValue = "true", matchIfMissing = true)
@Slf4j
public class KafkaConsumerConfiguration {

    public KafkaConsumerConfiguration(DataRouter dataRouter,
                                      DefaultServerConfig defaultServerConfig,
                                      SpringELExpressionService expressionService) {
        this.dataRouter = dataRouter;
        this.defaultServerConfig = defaultServerConfig;
        this.expressionService = expressionService;
    }

    private final DataRouter dataRouter;

    private final DefaultServerConfig defaultServerConfig;

    private final SpringELExpressionService expressionService;

    @Bean
    public BatchMessageListener<Long, byte[]> batchMessageListener(){
        return new RealTimeDataConsumer(defaultServerConfig, dataRouter, expressionService);
    }

    @Bean
    ConsumerFactory<Long, Message> consumerFactory(){
        KafkaConfig.ConsumerConfig config = defaultServerConfig.getKafka().getConsumer();
        config.setProperties(config.getProperties().entrySet().stream()
                .collect(Collectors.toMap((e) -> e.getKey().replace('-','.'), Map.Entry::getValue)));
        log.info("canal kafka consume config: {}", config.getProperties());
        return new DefaultKafkaConsumerFactory<>(config.getProperties());
    }

    @Bean
    ContainerProperties containerProperties(){
        KafkaConfig.ContainerConfig containerConfig = defaultServerConfig.getKafka().getContainer();
        List<String> topics = containerConfig.getTopics();
        ContainerProperties containerProperties = new ContainerProperties(topics.toArray(new String[0]));
        containerProperties.setAckMode(containerConfig.getAckMode());
        containerProperties.setMessageListener(batchMessageListener());
        containerProperties.setCommitCallback((offsets, exception) -> log.info("commit offset {}", offsets));
        return containerProperties;
    }

    @Bean
    KafkaMessageListenerContainer<Long,Message> messageListenerContainer(){
        return new KafkaMessageListenerContainer<>(consumerFactory(), containerProperties());
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<Long,Message> kafkaListenerContainerFactory(){
        ConcurrentKafkaListenerContainerFactory<Long,Message> containerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        containerFactory.setConsumerFactory(consumerFactory());
        return containerFactory;
    }

}
