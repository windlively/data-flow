package ink.andromeda.dataflow.core.node.resolver;

import ink.andromeda.dataflow.core.SourceEntity;
import ink.andromeda.dataflow.core.SpringELExpressionService;
import ink.andromeda.dataflow.core.TransferEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

public class EvalContextResolver extends DefaultConfigurationResolver {

    public EvalContextResolver(SpringELExpressionService expressionService) {
        super(expressionService);
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
            if(StringUtils.isNotEmpty(exp)){
                value = expressionService.executeExpression(exp);
            }else {
                throw new IllegalArgumentException("");
            }
            rootData.put(name, value);
        });
    }
}
