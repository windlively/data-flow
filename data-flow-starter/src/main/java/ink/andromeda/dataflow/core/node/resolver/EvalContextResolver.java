package ink.andromeda.dataflow.core.node.resolver;

import ink.andromeda.dataflow.core.SourceEntity;
import ink.andromeda.dataflow.core.SpringELExpressionService;
import ink.andromeda.dataflow.core.TransferEntity;
import ink.andromeda.dataflow.datasource.DynamicDataSource;
import ink.andromeda.dataflow.datasource.dao.CommonJdbcDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * eval_context
 * 此配置项为当前节点提供必要的参数, 相当于变量定义, 来源可为sql或自定义表达式以及service
 */
public class EvalContextResolver extends DefaultConfigurationResolver {

    private final DynamicDataSource dataSource;

    private final CommonJdbcDao commonDao;

    public EvalContextResolver(SpringELExpressionService expressionService,
                               DynamicDataSource dataSource,
                               CommonJdbcDao commonDao) {
        super(expressionService);
        this.dataSource = dataSource;
        this.commonDao = commonDao;
    }

    @Override
    public String getName() {
        return "eval_context";
    }

    @Override
    public void resolve(SourceEntity sourceEntity, TransferEntity transferEntity, Object config, Map<String, Object> rootData) throws Exception {
        if(config == null) return;
        Assert.isTrue(config instanceof List, "eval_context must be list type");

        //noinspection unchecked
        ((List<Map<String, Object>>) (config)).forEach(dataItem -> {
            String name = (String) dataItem.get("name");
            Object value;
            String exp = (String) dataItem.get("expression");
            String sql = (String) dataItem.get("sql");
            if(StringUtils.isNotBlank(exp)){
                value = expressionService.executeExpression(exp);
            }else if(StringUtils.isNotBlank(sql)){
                String type = (String) Objects.requireNonNull(dataItem.get("type"), "result type is required with sql");
                String dataSourceName = (String) dataItem.get("data_source");
                value = commonDao.select(sql, dataSourceName, type);
            } else {
                throw new IllegalArgumentException("");
            }
            rootData.put(name, value);
        });
    }
}
