package ink.andromeda.dataflow.datasource;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Slf4j
public class DataSourceDetermineAspect {

    private final DynamicDataSource dynamicDataSource;

    public DataSourceDetermineAspect(DynamicDataSource dynamicDataSource) {
        this.dynamicDataSource = dynamicDataSource;
    }

    // 动态切换数据源
    @Around("@annotation(ink.andromeda.dataflow.datasource.SwitchSource)")
    public Object around(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        SwitchSource switchSource = method.getAnnotation(SwitchSource.class);
        String name = switchSource.value().value();
        // 如果name项不为空, 则使用name指定的数据源
        if(!"".equals(switchSource.name()))
            name = switchSource.name();
        dynamicDataSource.changeLookupKey(name);
        log.info("use datasource {}", name);
        Object result = null;
        try {
            // 执行原方法
            result = joinPoint.proceed();

        } catch (Throwable throwable) {
            log.error("data source determine aspect exception: {}", throwable.getMessage(), throwable);
            throwable.printStackTrace();
        }finally {
            // 重置为默认数据源
            dynamicDataSource.resetToDefault();
        }

        return result;
    }

}
