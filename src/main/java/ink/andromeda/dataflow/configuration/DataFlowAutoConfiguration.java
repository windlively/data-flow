package ink.andromeda.dataflow.configuration;

import ink.andromeda.dataflow.core.DataFlowManager;
import ink.andromeda.dataflow.core.DefaultDataFlowManager;
import ink.andromeda.dataflow.core.Registry;
import ink.andromeda.dataflow.core.SimpleRegistry;
import ink.andromeda.dataflow.core.converter.configuarion.SpringELConfigurationResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;

@Configuration
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
                                    RedisTemplate<String, String> redisTemplate){
        return new DefaultDataFlowManager(mongoTemplate, redisTemplate);
    }


}
