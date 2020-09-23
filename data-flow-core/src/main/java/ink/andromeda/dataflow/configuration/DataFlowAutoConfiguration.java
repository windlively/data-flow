package ink.andromeda.dataflow.configuration;

import ink.andromeda.dataflow.core.DataFlowManager;
import ink.andromeda.dataflow.core.DefaultDataFlowManager;
import ink.andromeda.dataflow.core.Registry;
import ink.andromeda.dataflow.core.SimpleRegistry;
import ink.andromeda.dataflow.core.converter.configresolver.SpringELConfigurationResolver;
import ink.andromeda.dataflow.entity.RefreshCacheMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import static ink.andromeda.dataflow.util.GeneralTools.GSON;

@Configuration
@Slf4j
public class DataFlowAutoConfiguration {

    @ConditionalOnMissingBean(name = "flowNodeConvertResolver")
    @Bean
    Registry<SpringELConfigurationResolver> flowNodeConvertResolver(){
        SimpleRegistry<SpringELConfigurationResolver> registry = new SimpleRegistry<>();

        return registry;
    }

    @Bean
    @ConditionalOnMissingBean(name = "flowNodeExportResolver")
    Registry<SpringELConfigurationResolver> flowNodeExportResolver(){
        SimpleRegistry<SpringELConfigurationResolver> registry = new SimpleRegistry<>();

        return registry;
    }

    @Bean
    @ConditionalOnMissingBean(name = "dataFlowManager")
    DataFlowManager dataFlowManager(MongoTemplate mongoTemplate,
                                    RedisTemplate<String, String> redisTemplate,
                                    Registry<SpringELConfigurationResolver> flowNodeConvertResolver,
                                    Registry<SpringELConfigurationResolver> flowNodeExportResolver){
        return new DefaultDataFlowManager(mongoTemplate, redisTemplate,
                flowNodeConvertResolver, flowNodeExportResolver);
    }

    @Bean
    @ConditionalOnBean(DefaultDataFlowManager.class)
    @DependsOn("dataFlowManager")
    RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory,
                                                                DataFlowManager dataFlowManager){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener((message, pattern) -> {
            String body = new String(message.getBody());
            log.info("receive redis message, topic: {}, body: {}", new String(message.getChannel()), body);
            RefreshCacheMessage refreshCacheMessage = GSON().fromJson(body, RefreshCacheMessage.class);
            switch (refreshCacheMessage.getCacheType()){
                case "flow-config":
                    ((DefaultDataFlowManager)dataFlowManager).reload(true);
            }


        }, new ChannelTopic("refresh-cache"));
        return container;

    }


}
