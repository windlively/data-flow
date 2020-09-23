package ink.andromeda.dataflow.configuration;

import ink.andromeda.dataflow.interceptor.HttpInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
@EnableScheduling
public class AppConfiguration implements WebMvcConfigurer {

    private final RedisTemplate<String, String> strRedisTemplate;


    public AppConfiguration(RedisTemplate<String, String> strRedisTemplate) {

        this.strRedisTemplate = strRedisTemplate;

    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HttpInterceptor()).addPathPatterns("/**");
    }

    @Bean
    public Docket restApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .title("core system event")
                        .version("0.0.1")
                        .description("核心系统数据同步与事件推断")
                        .build())
                .select()
                .apis(RequestHandlerSelectors.basePackage("net.abakus.coresystem.data.web.controller"))
                .paths(PathSelectors.any())
                .build();
    }

}
