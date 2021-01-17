package ink.windlively.dataflow.demo;

import ink.windlively.dataflow.core.Registry;
import ink.windlively.dataflow.core.SpringELExpressionService;
import ink.windlively.dataflow.core.flow.DataFlowManager;
import ink.windlively.dataflow.core.node.resolver.DefaultConfigurationResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.yaml.snakeyaml.Yaml;
import redis.embedded.RedisServer;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.io.IOException;

@SpringBootApplication(
        exclude = {
                RabbitAutoConfiguration.class,
                MongoAutoConfiguration.class
        },
        scanBasePackages = "ink.windlively.dataflow"
)
@Slf4j
@EnableScheduling
@EnableSwagger2
public class DataFlowDemoApplication implements DisposableBean, InitializingBean {

    static RedisServer redisServer;

    private static void initEmbeddedRedisServer() throws IOException {
        Yaml yaml = new Yaml();
        Object load = yaml.load(DataFlowDemoApplication.class.getResourceAsStream("/application.yml"));
        SpelExpressionParser parser = new SpelExpressionParser();
        int port = 6379;
        try {
            //noinspection ConstantConditions
            port = parser.parseExpression("[spring][redis][port]").getValue(load, Integer.class);
        }catch (Exception ex){
            log.info("not found spring.redis.port config, embedded redis port is set to 6379");
        }

        redisServer = RedisServer.builder()
                .port(port)
                //.redisExecProvider(customRedisExec) //com.github.kstyrc (not com.orange.redis-embedded)
                .setting("maxmemory 128M") //maxheap 128M
                .build();
        redisServer.start();
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DataFlowDemoApplication.class);
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

    @Override
    public void destroy() throws Exception {
        redisServer.stop();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initEmbeddedRedisServer();
    }
}
