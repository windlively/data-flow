package ink.andromeda.dataflow;

import com.zaxxer.hikari.HikariDataSource;
import ink.andromeda.dataflow.core.*;
import ink.andromeda.dataflow.core.flow.DataFlowManager;
import ink.andromeda.dataflow.core.flow.DefaultDataFlowManager;
import ink.andromeda.dataflow.core.mq.*;
import ink.andromeda.dataflow.core.node.resolver.*;
import ink.andromeda.dataflow.datasource.DataSourceConfig;
import ink.andromeda.dataflow.datasource.DataSourceDetermineAspect;
import ink.andromeda.dataflow.datasource.DynamicDataSource;
import ink.andromeda.dataflow.datasource.dao.CommonJdbcDao;
import ink.andromeda.dataflow.datasource.dao.DefaultCommonJdbcDao;
import ink.andromeda.dataflow.entity.RefreshCacheMessage;
import ink.andromeda.dataflow.util.GeneralTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ink.andromeda.dataflow.util.GeneralTools.GSON;
import static ink.andromeda.dataflow.util.GeneralTools.calcSum;

@Configuration
@ConditionalOnProperty(name = "data-flow.enable", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(DataFlowProperties.class)
@Slf4j
public class DataFlowAutoConfiguration {

    private final DataFlowProperties dataFlowProperties;

    public DataFlowAutoConfiguration(DataFlowProperties dataFlowProperties) {
        this.dataFlowProperties = dataFlowProperties;
    }

    @ConditionalOnMissingBean(name = "flowNodeResolver", value = Registry.class)
    @Bean
    Registry<DefaultConfigurationResolver> flowNodeConvertResolver(SpringELExpressionService expressionService,
                                                                   CommonJdbcDao commonJdbcDao,
                                                                   MessageQueueContainer messageQueueContainer
    ) {
        SimpleRegistry<DefaultConfigurationResolver> registry = new SimpleRegistry<>();
        registry.addLast(new EvalContextResolver(expressionService, commonJdbcDao));
        registry.addLast(new FilterResolver(expressionService));
        registry.addLast(new SimpleConvertResolver(expressionService));
        registry.addLast(new SimpleCopyFieldsResolver(expressionService));
        registry.addLast(new ConditionalExpressionResolver(expressionService));
        registry.addLast(new AdditionalExpressionResolver(expressionService));
        registry.addLast(new ExportToRDBResolver(expressionService, commonJdbcDao));
        registry.addLast(new ExportToKafkaResolver(expressionService, messageQueueContainer));
        registry.effect();
        return registry;
    }

    @Bean("dataFlowManager")
    @ConditionalOnMissingBean(name = "dataFlowManager")
    DefaultDataFlowManager dataFlowManager(MongoTemplate mongoTemplate,
                                           RedisTemplate<String, String> redisTemplate,
                                           Registry<DefaultConfigurationResolver> flowNodeResolver) {
        return new DefaultDataFlowManager(mongoTemplate, redisTemplate,
                flowNodeResolver);
    }

    @Bean
    @ConditionalOnBean(DefaultDataFlowManager.class)
    RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory,
                                                                DataFlowManager dataFlowManager) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener((message, pattern) -> {
            String body = new String(message.getBody());
            log.info("receive redis message, topic: {}, body: {}", new String(message.getChannel()), body);
            RefreshCacheMessage refreshCacheMessage = GSON().fromJson(body, RefreshCacheMessage.class);
            switch (refreshCacheMessage.getCacheType()) {
                case "flow-config":
                    ((DefaultDataFlowManager) dataFlowManager).reload(true);
            }
        }, new ChannelTopic("refresh-cache"));
        return container;
    }

    @Bean
    @ConditionalOnMissingBean
    MessageQueueContainer messageQueueContainer() {
        MessageQueueContainer container = new SimpleMessageQueueContainer();
        Optional.ofNullable(dataFlowProperties.getMqInstances()).ifPresent(mqInstanceConfigs -> {
            Stream.of(mqInstanceConfigs).map(c -> {
                try {

                    String type = c.getType() == null ?
                            dataFlowProperties.getDefaultMqType() : c.getType();
                    String name = Objects.requireNonNull(c.getName(), "must assign a name for mq instance");
                    switch (type) {
                        case "kafka":
                            return new KafkaInstance(
                                    c.getProperties().entrySet().stream().collect(
                                            Properties::new,
                                            (p, e1) -> p.put(((String) e1.getKey()).replace('-', '.'), e1.getValue()),
                                            Hashtable::putAll
                                    ),
                                    name
                            );
                        case "rocket":
                            Properties properties = c.getProperties();
                            return new RocketInstance(
                                    name,
                                    properties.getProperty("group-id"),
                                    properties.getProperty("name-serv")
                            );
                        default:
                            throw new IllegalArgumentException("unknown mq type: " + type);
                    }
                } catch (Exception ex) {
                    throw new IllegalStateException(String.format(
                            "exception on create rocket instance: %s, %s",
                            c.getName(), ex.getMessage()));

                }

            }).forEach(container::add);
        });

        return container;
    }

    @Bean
    @ConditionalOnMissingBean
    DataRouter dataRouter(DataFlowManager dataFlowManager) {
        return new DefaultDataRouter(dataFlowManager);
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

        public MultiDataSourceConfiguration(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

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
        public CommonJdbcDao commonDao(DynamicDataSource dynamicDataSource) {
            return new DefaultCommonJdbcDao(dynamicDataSource);
        }

        @Bean
        public DynamicDataSource dataSource() {
            Map<String, DataSource> dataSourceMap = new HashMap<>();
            Stream.of(datasourceConfig().getHikari()).forEach(config -> {
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
        public DataSourceDetermineAspect dataSourceDetermineAspect() {
            return new DataSourceDetermineAspect(dataSource());
        }

        @Bean
        @DependsOn("dataSource")
        public Map<String, DataSource> dataSourceMap() {
            Map<String, DataSource> dataSourceMap = new HashMap<>();
            dataSource().getIncludedDataSource().forEach((name, source) -> dataSourceMap.put((String) name, source));
            DATASOURCE_SCHEME_NAME_REF.forEach((dataSourceName, dbNames) -> Stream.of(dbNames).forEach(dbName -> dataSourceMap.put(dbName, dataSourceMap.get(dataSourceName))));
            log.info("data source name and database name reference: {}", dataSourceMap);
            return dataSourceMap;
        }

        @Bean
        public PlatformTransactionManager transactionManager() {
            return new DataSourceTransactionManager(dataSource());
        }

        @Bean
        @ConfigurationProperties("multi-data-source.data-source")
        public DataSourceConfig datasourceConfig() {
            return new DataSourceConfig();
        }

        private final ApplicationContext applicationContext;

        @Bean
        @ConditionalOnMissingBean(ExpressionService.class)
        SpringELExpressionService springELExpressionService() {
            return new SpringELExpressionService(applicationContext, dataSourceMap());
        }

    }

}
