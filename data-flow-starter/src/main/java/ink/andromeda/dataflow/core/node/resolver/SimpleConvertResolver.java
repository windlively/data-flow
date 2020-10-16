package ink.andromeda.dataflow.core.node.resolver;

import ink.andromeda.dataflow.core.SourceEntity;
import ink.andromeda.dataflow.core.SpringELExpressionService;
import ink.andromeda.dataflow.core.TransferEntity;

import java.util.Map;

public class SimpleConvertResolver extends DefaultConfigurationResolver{

    public SimpleConvertResolver(SpringELExpressionService expressionService) {
        super(expressionService);
    }

    @Override
    public String getName() {
        return "simple_convert";
    }

    @Override
    public void resolve(SourceEntity source, TransferEntity input, TransferEntity target, Object config, Map<String, Object> rootData) throws Exception {
        if(config == null) return;
        checkConfigType(config, Map.class, "Map<String, String>");
        //noinspection unchecked
        ((Map<String, String>) config).forEach((field, exp) -> {
            target.getData().put(field, expressionService.executeExpression(exp));
        });
    }
}
