package ink.andromeda.dataflow.service;

import ink.andromeda.dataflow.util.CommonUtils;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import ink.andromeda.dataflow.datasource.dao.CommonDao;
import ink.andromeda.dataflow.datasource.dao.CoreTableDao;
import ink.andromeda.dataflow.util.Functions;
import ink.andromeda.dataflow.entity.ColumnInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class ExpressionService {

    private static final String masterSchemaName = "abakus_core";

    // spring context
    private final ApplicationContext applicationContext;

    // 项目中的所有数据源
    public final Map<String, DataSource> dataSourceMap;

    private final CommonDao commonDao;

    private final Map<String, List<ColumnInfo>> coreTableSchema = new ConcurrentHashMap<>();

    private final CoreTableDao coreTableDao;
    // EvaluationContext非线程安全, 但是创建代价较为昂贵, 因此为每个线程创建一个
    // 使用时作为方法参数传入, 不能保存在实例属性中, 保证其安全性, 不能共享
    private final ThreadLocal<StandardEvaluationContext> evaluationContext;

    public ExpressionService(ApplicationContext applicationContext,
                             @Qualifier("dataSourceMap") Map<String, DataSource> dataSourceMap,
                             CommonDao commonDao, CoreTableDao coreTableDao) {

        this.applicationContext = applicationContext;
        this.dataSourceMap = dataSourceMap;
        this.commonDao = commonDao;
        this.coreTableDao = coreTableDao;

        // Evaluation Context 配置
        this.evaluationContext = ThreadLocal.withInitial(() -> {
            StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
            try {
                // 通用函数注册
                evaluationContext.registerFunction("toString", CommonUtils.class.getMethod("toString", Object.class));
                evaluationContext.registerFunction("mergeObject",CommonUtils.class.getMethod("mergeObject", Object.class, Object.class));
                evaluationContext.registerFunction("sum",CommonUtils.class.getMethod("calcSum", BigDecimal[].class));
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
        coreTableSchema.putAll(coreTableDao.findAllColumns().stream().collect(Collectors.groupingBy(ColumnInfo::getColumnName)));
    }

    public List<ColumnInfo> getColumnInfo(String tableName){
        return coreTableSchema.computeIfAbsent(tableName, k -> coreTableDao.findAllColumns(tableName, masterSchemaName));
    }

    public void refreshColumnInfoCache(){
        coreTableSchema.putAll(coreTableDao.findAllColumns().stream().collect(Collectors.groupingBy(ColumnInfo::getColumnName)));
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

    public Object executeExpression(String expression, StandardEvaluationContext evaluationContext, boolean template) {
        try {
            if (template)
                return expressionParser().parseExpression(expression, parserContext).getValue(evaluationContext);
            return expressionParser().parseExpression(expression).getValue(evaluationContext);
        }catch (Exception ex){
            throw new RuntimeException("execute expression [" + expression +"] failed: " + ex.getMessage(), ex);
        }
    }

    public <T> T executeExpression(String expression, StandardEvaluationContext evaluationContext, Class<T> clazz, boolean template) {
        try {
            if (template)
                return expressionParser().parseExpression(expression, parserContext).getValue(evaluationContext, clazz);
            return expressionParser().parseExpression(expression).getValue(evaluationContext, clazz);
        }catch (Exception ex){
            throw new RuntimeException("execute expression [" + expression +"] failed: " + ex.getMessage(), ex);
        }
    }

    public Object executeExpression(String expression, StandardEvaluationContext evaluationContext) {
        return executeExpression(expression, evaluationContext, false);
    }

    public <T> T executeExpression(String expression, StandardEvaluationContext evaluationContext, Class<T> clazz) {
        return executeExpression(expression, evaluationContext, clazz, false);
    }

    public Object executeExpression(String expression, StandardEvaluationContext evaluationContext, Object rootObject) {
        return expressionParser().parseExpression(expression).getValue(evaluationContext, rootObject);
    }

    public <T> T executeExpression(String expression, StandardEvaluationContext evaluationContext, Object rootObject, Class<T> clazz) {
        return expressionParser().parseExpression(expression).getValue(evaluationContext, rootObject, clazz);
    }

    /**
     * 生成数据项
     *
     * @param dataItem 该数据项配置
     * @return 数据项的值
     */
    public Object genEvalContextVal(Map<String, Object> dataItem, StandardEvaluationContext context) {

        // 自定义数据, 忽略其他所有配置, 支持任何有返回值的表达式
        String customData = (String) dataItem.get("custom_data");
        if (StringUtils.isNotEmpty(customData))
            return executeExpression(customData, context);

        // 调用预先定义的获取数据的服务
        String service = (String) dataItem.get("service");
        if (StringUtils.isNotEmpty(service)) {
            // TODO 其他数据服务
        }

        // SQL方式查询数据
        String type = (String) dataItem.get("type");
        String sql = (String) dataItem.get("sql");

        String dataSourceName = (String) dataItem.get("data_source");
        if(StringUtils.isEmpty(dataSourceName))
            dataSourceName = (String) dataItem.get("db_name");

        String tableName = (String) dataItem.get("t_name");
        if (sql == null) {
            sql = String.format("SELECT * FROM %s.%s WHERE %s", dataSourceName, tableName, dataItem.get("where_case"));
        }

        if(StringUtils.isEmpty(dataSourceName)) {
            // 获取SQL中所用到的数据库，
            Matcher matcher = CommonUtils.SQL_TABLE_NAME_REGEX.matcher(sql);
            if (matcher.find()) {
                String tName = matcher.group();
                dataSourceName = tName.trim().split("\\.")[0].trim();
            }
        }

        if(StringUtils.isEmpty(dataSourceName))
            throw new RuntimeException(String.format("sql [%s] data source name could not found!", sql));
        // 解析sql中的变量
        sql = executeExpression(sql, context, String.class, true);
        return commonDao.select(sql, dataSourceName, type);
    }

}
