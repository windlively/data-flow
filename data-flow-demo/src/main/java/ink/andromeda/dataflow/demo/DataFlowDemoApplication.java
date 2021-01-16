package ink.andromeda.dataflow.demo;

import com.google.common.collect.Lists;
import ink.andromeda.dataflow.core.Registry;
import ink.andromeda.dataflow.core.SpringELExpressionService;
import ink.andromeda.dataflow.core.flow.DataFlowManager;
import ink.andromeda.dataflow.core.node.resolver.DefaultConfigurationResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Properties;

@SpringBootApplication(
        exclude = {
                RabbitAutoConfiguration.class,
                MongoAutoConfiguration.class,
                RedisAutoConfiguration.class
        },
        scanBasePackages = "ink.andromeda.dataflow"
)
@Slf4j
@EnableScheduling
@EnableSwagger2
public class DataFlowDemoApplication {

    public static void main(String[] args) {
        String env = System.getProperty("env");
        if (!Lists.newArrayList("FAT", "UAT", "PRO").contains(env)) {
            log.warn("no env arg found, set default to FAT");
            env = "FAT";
        }

        Properties properties = new Properties();
        properties.setProperty("spring.profiles.active", env);

        SpringApplication app = new SpringApplication(DataFlowDemoApplication.class);
        app.setDefaultProperties(properties);
        app.run(args);
    }

    @Bean
    DataFlowManager dataFlowManager(Registry<DefaultConfigurationResolver> nodeConfigResolver,
                                    SpringELExpressionService springELExpressionService){
        return new LocalConfigDataFlowManager(nodeConfigResolver, springELExpressionService);
    }

    @Bean
    KafkaListenerErrorHandler kafkaListenerErrorHandler(){
        return (message, exception) -> {
            log.error("exception in listen kafka message: {}, body: {}", exception.getMessage(), message.getPayload(), exception);
            return null;
        };
    }
}
