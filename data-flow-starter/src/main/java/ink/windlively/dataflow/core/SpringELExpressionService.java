package ink.windlively.dataflow.core;

import ink.windlively.dataflow.util.Functions;
import ink.windlively.dataflow.util.GeneralTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
public class SpringELExpressionService implements ExpressionService<String> {

    // spring context
    private final ApplicationContext applicationContext;

    // 项目中的所有数据源
    public final Map<String, DataSource> dataSourceMap;

    // EvaluationContext非线程安全, 但是创建代价较为昂贵, 因此为每个线程创建一个
    // 使用时作为方法参数传入, 不能保存在实例属性中, 保证其安全性, 不能共享
    private final ThreadLocal<StandardEvaluationContext> evaluationContext;

    public SpringELExpressionService(ApplicationContext applicationContext,
                                     @Qualifier("dataSourceMap") Map<String, DataSource> dataSourceMap) {

        this.applicationContext = applicationContext;
        this.dataSourceMap = dataSourceMap;

        // Evaluation Context 配置
        this.evaluationContext = ThreadLocal.withInitial(() -> {
            StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
            try {
                // 通用函数注册
                evaluationContext.registerFunction("toString", GeneralTools.class.getMethod("toString", Object.class));
                evaluationContext.registerFunction("mergeObject", GeneralTools.class.getMethod("mergeObject", Object.class, Object.class));
                evaluationContext.registerFunction("sum", GeneralTools.class.getMethod("calcSum", BigDecimal[].class));
                Stream.of(Functions.class.getMethods()).filter(method -> Modifier.isStatic(method.getModifiers()))
                        .forEach(method -> evaluationContext.registerFunction(method.getName(), method));
            } catch (NoSuchMethodException e) {
                log.error(e.toString(), e);
                e.printStackTrace();
            }
            // 设置bean解析器, 可调用spring bean
            evaluationContext.setBeanResolver((context, beanName) -> this.applicationContext.getBean(beanName));
            return evaluationContext;

        });

    }

    private final ThreadLocal<SpelExpressionParser> expressionParser = ThreadLocal.withInitial(() -> {
        // 混合模式, 提高部分表达式的执行速度
        SpelParserConfiguration configuration = new SpelParserConfiguration(SpelCompilerMode.MIXED, Thread.currentThread().getContextClassLoader());
        return new SpelExpressionParser(configuration);
    });

    // 字符串模板
    private final ParserContext parserContext = new ParserContext() {
        @Override
        public boolean isTemplate() {
            return true;
        }

        @Override
        public String getExpressionPrefix() {
            return "${";
        }

        @Override
        public String getExpressionSuffix() {
            return "}";
        }
    };

    public StandardEvaluationContext evaluationContext() {
        return evaluationContext.get();
    }

    public SpelExpressionParser expressionParser() {
        return expressionParser.get();
    }

    @PostConstruct
    public void init() {

    }

    @Nullable
    public Object executeExpression(String expression, StandardEvaluationContext evaluationContext, boolean template) {
        try {
            if (template)
                return expressionParser().parseExpression(expression, parserContext).getValue(evaluationContext);
            return expressionParser().parseExpression(expression).getValue(evaluationContext);
        } catch (Exception ex) {
            throw new RuntimeException("execute expression [" + expression + "] failed: " + ex.getMessage(), ex);
        }
    }

    @Nullable
    public Object executeExpression(String expression, boolean template) {
        return executeExpression(expression, evaluationContext(), template);
    }

    @Nullable
    public <T> T executeExpression(String expression, StandardEvaluationContext evaluationContext, Class<T> clazz, boolean template) {
        try {
            if (template)
                return expressionParser().parseExpression(expression, parserContext).getValue(evaluationContext, clazz);
            return expressionParser().parseExpression(expression).getValue(evaluationContext, clazz);
        } catch (Exception ex) {
            throw new RuntimeException("execute expression [" + expression + "] failed: " + ex.getMessage(), ex);
        }
    }

    @Nullable
    public <T> T executeExpression(String expression, Class<T> clazz, boolean template) {
        return executeExpression(expression, evaluationContext(), clazz, template);
    }

    @Nullable
    public Object executeExpression(String expression, StandardEvaluationContext evaluationContext) {
        return executeExpression(expression, evaluationContext, false);
    }

    @Nullable
    public Object executeExpression(String expression) {
        return executeExpression(expression, evaluationContext(), false);
    }

    @Nullable
    public <T> T executeExpression(String expression, StandardEvaluationContext evaluationContext, Class<T> clazz) {
        return executeExpression(expression, evaluationContext, clazz, false);
    }

    @Nullable
    public <T> T executeExpression(String expression, Class<T> clazz) {
        return executeExpression(expression, evaluationContext(), clazz, false);
    }

    @Nullable
    public Object executeExpression(String expression, StandardEvaluationContext evaluationContext, Object rootObject) {
        return expressionParser().parseExpression(expression).getValue(evaluationContext, rootObject);
    }

    @Nullable
    public <T> T executeExpression(String expression, StandardEvaluationContext evaluationContext, Object rootObject, Class<T> clazz) {
        return expressionParser().parseExpression(expression).getValue(evaluationContext, rootObject, clazz);
    }

    @Override
    public <R> R executeExpression(String expression, @Nullable EnvironmentContext additionalEnv, Class<R> clazz) {
        Assert.notNull(additionalEnv, "environment context is null");
        return executeExpression(
                expression,
                (StandardEvaluationContext) Objects.requireNonNull(additionalEnv.getVariable("evaluationContext")),
                clazz);

    }
}
