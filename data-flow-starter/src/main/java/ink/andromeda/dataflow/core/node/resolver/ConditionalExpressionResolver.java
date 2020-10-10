package ink.andromeda.dataflow.core.node.resolver;

import ink.andromeda.dataflow.core.SourceEntity;
import ink.andromeda.dataflow.core.SpringELExpressionService;
import ink.andromeda.dataflow.core.TransferEntity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ConditionalExpressionResolver extends DefaultConfigurationResolver{

    public ConditionalExpressionResolver(SpringELExpressionService expressionService) {
        super(expressionService);
    }

    @Override
    public String getName() {
        return "conditional_expression";
    }

    @Override
    public void resolve(SourceEntity source, TransferEntity input, TransferEntity target, Object config, Map<String, Object> rootData) throws Exception {
        if(config == null)
            return;
        checkConfigType(config, List.class, "List<Map<String, Object>>");
        //noinspection unchecked
        ((List<Map<String, Object>>)config).forEach(this::processConditional);
    }

    // 递归执行条件语句
    private void processConditional(Map<String, Object> conditionalItem) {
        String condition = (String) conditionalItem.get("condition");

        if (Objects.requireNonNull(expressionService.executeExpression(condition, boolean.class))) {
            Object expression = conditionalItem.get("expression");
            if (expression instanceof String) {
                expressionService.executeExpression((String) expression);
                return;
            }
            if (expression instanceof List) {
                //noinspection unchecked
                ((List<Object>) expression).forEach(exp -> {
                    if (exp instanceof String) {
                        expressionService.executeExpression((String) exp);
                        return;
                    }
                    if (exp instanceof Map) {
                        //noinspection unchecked
                        processConditional((Map<String, Object>) exp);
                    }
                });
            }
        }
    }
}
