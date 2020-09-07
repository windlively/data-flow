package ink.andromeda.dataflow.configuration;

import lombok.extern.slf4j.Slf4j;
import net.abakus.coresystem.entity.config.RedisConfig;
import net.abakus.coresystem.redis.RedisClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RedisConfiguration {

    public final static String AppKeyPrefix = "cs-datachannel:";

    // redis客户端配置:
    @Bean
    RedisClient redisClient(@Qualifier("redisConfig") RedisConfig redisConfig){
        return RedisClient.build(redisConfig);
    }

    @Bean
    @ConfigurationProperties(prefix = "redis.config")
    RedisConfig redisConfig(){
        return new RedisConfig();
    }
}
