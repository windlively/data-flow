package ink.andromeda.dataflow.configuration;

import ink.andromeda.dataflow.redis.RedisClient;
import ink.andromeda.dataflow.redis.RedisConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RedisConfiguration {

    public final static String AppKeyPrefix = "cs-datachannel:";

}
