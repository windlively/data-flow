package ink.windlively.dataflow.server;

import com.google.common.collect.Lists;
import ink.windlively.dataflow.interceptor.HttpInvokeInterceptor;
import ink.windlively.dataflow.server.entity.DefaultServerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Properties;

@Slf4j
@SpringBootApplication(scanBasePackages = {
        "ink.windlively.dataflow"
}, exclude = {
        RabbitAutoConfiguration.class,
        DataSourceAutoConfiguration.class
})
@EnableConfigurationProperties(DefaultServerConfig.class)
@EnableSwagger2
public class DataFlowApplication implements WebMvcConfigurer {

    public DataFlowApplication(DefaultServerConfig defaultServerConfig) {
        this.defaultServerConfig = defaultServerConfig;
    }

    public static void main(String[] args) {
        String env = System.getProperty("env");
        if (!Lists.newArrayList("FAT", "UAT", "PRO").contains(env)) {
            log.warn("no env arg found, set default to FAT");
            env = "FAT";
        }

        Properties properties = new Properties();
        properties.setProperty("spring.profiles.active", env);

        SpringApplication app = new SpringApplication(DataFlowApplication.class);
        app.setDefaultProperties(properties);
        app.run(args);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

        }));
    }

    private final DefaultServerConfig defaultServerConfig;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(new HttpInvokeInterceptor(defaultServerConfig.isEnableHttpInvoke(),"server not enable http invoke"))
                .addPathPatterns("/**");
        if (!defaultServerConfig.isModifyConfigWithHttp()) {
            registry.addInterceptor(new HandlerInterceptor() {
                @Override
                public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
                    if (!request.getMethod().equals("GET"))
                        throw new IllegalStateException("You have no authority.");
                    return true;
                }
            }).addPathPatterns("/**");
        }
    }
}
