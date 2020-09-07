package ink.andromeda.dataflow;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Properties;

@Slf4j
@SpringBootApplication(scanBasePackages = {
        "ink.andromeda.dataflow"
}, exclude = {
        RabbitAutoConfiguration.class
})
@EnableSwagger2
public class CoreSystemEventApplication {

    public static void main(String[] args) {
        String env = System.getProperty("env");
        if (!Lists.newArrayList("FAT", "UAT", "PRO").contains(env)) {
            log.warn("no env arg found, set default to FAT");
            env = "PRO";
        }

        Properties properties = new Properties();
        properties.setProperty("spring.profiles.active", env);

        SpringApplication app = new SpringApplication(CoreSystemEventApplication.class);
        app.setDefaultProperties(properties);
        app.run(args);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            /*
                CS_SYNC_TASK_GROUP().shutdown();
                CS_EVENT_TASK_GROUP().shutdown();
                try {
                    CS_SYNC_TASK_GROUP().awaitTermination(10, TimeUnit.SECONDS);
                    CS_EVENT_TASK_GROUP().awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            */
        }));
    }
}
