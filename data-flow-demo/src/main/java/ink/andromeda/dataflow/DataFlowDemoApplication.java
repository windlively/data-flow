package ink.andromeda.dataflow;

import com.google.common.collect.Lists;
import ink.andromeda.dataflow.core.DataFlowManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Properties;

@SpringBootApplication
@Slf4j
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
    DataFlowManager dataFlowManager(){
        return new LocalConfigDataFlowManager();
    }
}
