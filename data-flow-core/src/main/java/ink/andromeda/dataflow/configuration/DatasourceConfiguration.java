package ink.andromeda.dataflow.configuration;

import com.zaxxer.hikari.HikariDataSource;
import ink.andromeda.dataflow.datasource.DataSourceConfig;
import ink.andromeda.dataflow.datasource.DynamicDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static ink.andromeda.dataflow.util.GeneralTools.setBeanProperties;
import static ink.andromeda.dataflow.util.GeneralTools.testDataSource;

@MapperScans({
        @MapperScan("net.abakus.coresystem.data.datasource.mapper"),
        @MapperScan("net.abakus.coresystem.mapper")
})
@Configuration
@Slf4j
public class DatasourceConfiguration {

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
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource());
        sessionFactoryBean.setTypeAliasesPackage("net.wecash.coresystem.data.entity");
        Objects.requireNonNull(sessionFactoryBean.getObject()).getConfiguration().setMapUnderscoreToCamelCase(true);
        return sessionFactoryBean.getObject();

    }

    @Bean
    public DynamicDataSource dataSource(){
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        Stream.of(datasourceConfig().getHikari()).forEach(config->{
            HikariDataSource dataSource = new HikariDataSource(config);
            String name = config.getPoolName();
            dataSource.setMaximumPoolSize(100);
            try {
                testDataSource(dataSource);
                dataSourceMap.put(name, dataSource);
                log.info("data source {} is ok", name);
            } catch (SQLException ex) {
                log.error("data source {} is invalid", name, ex);
            }

        });
        return new DynamicDataSource(dataSourceMap.get("master"), new HashMap<>(dataSourceMap));
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
