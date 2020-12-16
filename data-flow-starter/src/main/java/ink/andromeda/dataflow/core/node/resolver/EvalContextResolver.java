package ink.andromeda.dataflow.core.node.resolver;

import ink.andromeda.dataflow.core.SourceEntity;
import ink.andromeda.dataflow.core.SpringELExpressionService;
import ink.andromeda.dataflow.core.TransferEntity;
import ink.andromeda.dataflow.datasource.dao.CommonJdbcDao;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * eval_context
 * 此配置项为当前节点提供必要的参数, 相当于变量定义, 来源可为sql或自定义表达式以及service
 */
public class EvalContextResolver extends DefaultConfigurationResolver {

    private final CommonJdbcDao commonDao;

    public EvalContextResolver(SpringELExpressionService expressionService,
                               CommonJdbcDao commonDao) {
        super(expressionService);
        this.commonDao = commonDao;
    }

    @Override
    public String getName() {
        return "eval_context";
    }

    @Override
    public void resolve(SourceEntity source, TransferEntity input, TransferEntity target, Object config, Map<String, Object> rootData) throws Exception {
        if (config == null) return;
        checkConfigType(config, List.class, "List<Map<String, String>>");

        //noinspection unchecked
        for (Map<String, Object> dataItem : ((List<Map<String, Object>>) (config))) {
            String name = (String) Objects.requireNonNull(dataItem.get("name"), "must assign a name for eval_context item");
            Object value;
            String exp = (String) dataItem.get("expression");
            String sql = (String) dataItem.get("sql");
            String condition = (String) dataItem.get("on_condition");
            if (StringUtils.isNotBlank(condition)
                    && !Objects.requireNonNull(
                    expressionService.executeExpression(condition, expressionService.evaluationContext(), boolean.class))
            ) {
                continue;
            }
            if (StringUtils.isNotBlank(exp)) {
                value = expressionService.executeExpression(exp);
            } else if (StringUtils.isNotBlank(sql)) {
                String type = (String) Objects.requireNonNull(dataItem.get("type"), "result type is required with sql");
                String dataSourceName = (String) dataItem.get("data_source");
                sql = Objects.requireNonNull(expressionService.executeExpression(sql, String.class, true));
                value = commonDao.select(sql, dataSourceName, type);
            } else {
                throw new IllegalArgumentException("the eval_context item must has expression or sql config");
            }
            rootData.put(name, value);
        }
    }
}
