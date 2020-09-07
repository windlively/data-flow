package ink.andromeda.dataflow.configuration;

import com.alibaba.fastjson.JSONObject;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class CustomBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {


    private static List<Map<String, String>> datasourceConfig = new ArrayList<>();

    // BeanDefinitionRegistryPostProcessor执行时Spring Bean还未初始化, 需要手动读取配置文件

    static {
        try (
            InputStream inputStream = CustomBeanDefinitionRegistryPostProcessor.class.getResourceAsStream("/application.yml");
        ) {
            Yaml yaml = new Yaml();
            Map<String, Object> val = yaml.load(inputStream);
            JSONObject jsonObject = new JSONObject(val);
            //noinspection unchecked
            jsonObject.getJSONObject("datasource").getJSONArray("hikari").forEach(item-> datasourceConfig.add((Map<String, String>) item));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }

    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        // 动态注册DataSource Bean
        datasourceConfig.forEach(config->{
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(HikariDataSource.class);
            String beanName = config.get("name");
            beanDefinitionBuilder.addPropertyValue("username",config.get("username"));
            beanDefinitionBuilder.addPropertyValue("password", config.get("password"));
            beanDefinitionBuilder.addPropertyValue("driverClassName", config.get("driver-class-name"));
            beanDefinitionBuilder.addPropertyValue("jdbcUrl", config.get("jdbc-url"));
            registry.registerBeanDefinition(beanName, beanDefinitionBuilder.getRawBeanDefinition());
        });
    }



    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        System.out.println(Arrays.asList(beanFactory.getBeanDefinitionNames()));
    }
}
