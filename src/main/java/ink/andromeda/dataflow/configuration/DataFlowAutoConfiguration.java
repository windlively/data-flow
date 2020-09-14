package ink.andromeda.dataflow.configuration;

import ink.andromeda.dataflow.core.DataFlowManager;
import ink.andromeda.dataflow.core.Registry;
import ink.andromeda.dataflow.core.SimpleRegistry;
import ink.andromeda.dataflow.core.converter.configuarion.SpringELConfigurationResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    DataFlowManager dataFlowManager(){
        return null;
    }


}
