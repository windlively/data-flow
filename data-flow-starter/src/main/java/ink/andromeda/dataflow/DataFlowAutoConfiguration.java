package ink.andromeda.dataflow;

import com.zaxxer.hikari.HikariDataSource;
import ink.andromeda.dataflow.core.DataRouter;
import ink.andromeda.dataflow.core.DefaultDataRouter;
import ink.andromeda.dataflow.core.Registry;
import ink.andromeda.dataflow.core.SimpleRegistry;
import ink.andromeda.dataflow.core.flow.DataFlowManager;
import ink.andromeda.dataflow.core.flow.DefaultDataFlowManager;
import ink.andromeda.dataflow.core.node.resolver.DefaultConfigurationResolver;
import ink.andromeda.dataflow.datasource.DataSourceConfig;
import ink.andromeda.dataflow.datasource.DataSourceDetermineAspect;
import ink.andromeda.dataflow.datasource.DynamicDataSource;
import ink.andromeda.dataflow.datasource.dao.CommonJdbcDao;
import ink.andromeda.dataflow.datasource.dao.DefaultCommonJdbcDao;
import ink.andromeda.dataflow.entity.RefreshCacheMessage;
import ink.andromeda.dataflow.util.GeneralTools;
import ink.andromeda.dataflow.util.kafka.DataFlowKafkaListenerErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static ink.andromeda.dataflow.util.GeneralTools.GSON;

@Configuration
@ConditionalOnProperty(name = "data-flow.enable", havingValue = "true", matchIfMissing = true)
@Slf4j
public class DataFlowAutoConfiguration {

    @ConditionalOnMissingBean(name = "flowNodeConvertResolver", value = Registry.class)
    @Bean
    Registry<DefaultConfigurationResolver> flowNodeConvertResolver(){
        SimpleRegistry<DefaultConfigurationResolver> registry = new SimpleRegistry<>();

        return registry;
    }

    @Bean
    @ConditionalOnMissingBean(name = "flowNodeExportResolver", value = Registry.class)
    Registry<DefaultConfigurationResolver> flowNodeExportResolver(){
        SimpleRegistry<DefaultConfigurationResolver> registry = new SimpleRegistry<>();

        return registry;
    }

    @Bean("dataFlowManager")
    @ConditionalOnMissingBean(name = "dataFlowManager")
    DefaultDataFlowManager dataFlowManager(MongoTemplate mongoTemplate,
                                           RedisTemplate<String, String> redisTemplate,
                                           Registry<DefaultConfigurationResolver> flowNodeConvertResolver,
                                           Registry<DefaultConfigurationResolver> flowNodeExportResolver){
        return new DefaultDataFlowManager(mongoTemplate, redisTemplate,
                flowNodeConvertResolver, flowNodeExportResolver);
    }

    @Bean
    @ConditionalOnBean(DefaultDataFlowManager.class)
    RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory,
                                                                DataFlowManager dataFlowManager){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener((message, pattern) -> {
            String body = new String(message.getBody());
            log.info("receive redis message, topic: {}, body: {}", new String(message.getChannel()), body);
            RefreshCacheMessage refreshCacheMessage = GSON().fromJson(body, RefreshCacheMessage.class);
            switch (refreshCacheMessage.getCacheType()){
                case "flow-config":
                    ((DefaultDataFlowManager)dataFlowManager).reload(true);
            }
        }, new ChannelTopic("refresh-cache"));
        return container;

    }

    @Bean
    DataRouter dataRouter(DataFlowManager dataFlowManager){
        return new DefaultDataRouter(dataFlowManager);
    }

    @Bean
    @ConditionalOnMissingBean
    KafkaListenerErrorHandler kafkaListenerErrorHandler(){
        return new DataFlowKafkaListenerErrorHandler();
    }

    /**
     * 多数据源配置
     */
    @Configuration
    @ConditionalOnProperty(name = "multi-data-source.enable", havingValue = "true", matchIfMissing = true)
    @Slf4j
    static public class MultiDataSourceConfiguration {

        // k: 数据源名称, v: 所包含的数据库名称
        public static final Map<String, String[]> DATASOURCE_SCHEME_NAME_REF = new HashMap<>();

    /*
        改用Apollo配置, 不再读取本地配置文件
        static {
            try (
                    InputStream inputStream = CustomBeanDefinitionRegistryPostProcessor.class.getResourceAsStream("/application.yml");
            ) {
                Yaml yaml = new Yaml();
                Map<String, Object> val = yaml.load(inputStream);
                JSONObject datasourceConfig = new JSONObject(val);
                //noinspection unchecked
                datasourceConfig.getJSONObject("datasource").getJSONArray("hikari").forEach(item-> DatasourceConfiguration.datasourceConfig.add((Map<String, Object>) item));
                DatasourceConfiguration.datasourceConfig.forEach(config->{
                    HikariDataSource dataSource = new HikariDataSource();
                    Class<HikariDataSource> clazz = HikariDataSource.class;
                    String name = (String) config.get("pool-name");
                    setBeanProperties(config, dataSource,'-');
                    //noinspection rawtypes
                    DATASOURCE_SCHEME_NAME_REF.put(name, (String[]) ((List)config.get("all-dbs")).toArray(new String[0]));
                    DATA_SOURCE_MAP.put(name, dataSource);
                });
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                e.printStackTrace();
            }
        }
    */

        @Bean
        @ConditionalOnMissingBean
        public CommonJdbcDao commonDao(DynamicDataSource dynamicDataSource){
            return new DefaultCommonJdbcDao(dynamicDataSource);
        }

        @Bean
        public DynamicDataSource dataSource(){
            Map<String, DataSource> dataSourceMap = new HashMap<>();
            Stream.of(datasourceConfig().getHikari()).forEach(config->{
                HikariDataSource dataSource = new HikariDataSource(config);
                String name = config.getPoolName();
                dataSource.setMaximumPoolSize(100);
                try {
                    GeneralTools.testDataSource(dataSource);
                    dataSourceMap.put(name, dataSource);
                    log.info("data source {} is ok", name);
                } catch (SQLException ex) {
                    log.error("data source {} is invalid", name, ex);
                }

            });
            return new DynamicDataSource(dataSourceMap.get("master"), new HashMap<>(dataSourceMap));
        }

        @Bean
        public DataSourceDetermineAspect dataSourceDetermineAspect(){
            return new DataSourceDetermineAspect(dataSource());
        }

        @Bean
        @DependsOn("dataSource")
        public Map<String, DataSource> dataSourceMap(){
            Map<String, DataSource> dataSourceMap = new HashMap<>();
            dataSource().getIncludedDataSource().forEach((name, source) -> dataSourceMap.put((String) name, source));
            DATASOURCE_SCHEME_NAME_REF.forEach((dataSourceName, dbNames) -> Stream.of(dbNames).forEach(dbName -> dataSourceMap.put(dbName, dataSourceMap.get(dataSourceName))));
            log.info("data source name and database name reference: {}", dataSourceMap);
            return dataSourceMap;
        }

        @Bean
        public PlatformTransactionManager transactionManager(){
            return new DataSourceTransactionManager(dataSource());
        }

        @Bean
        @ConfigurationProperties("datasource")
        public DataSourceConfig datasourceConfig(){
            return new DataSourceConfig();
        }



    }

}
