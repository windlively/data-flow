package ink.andromeda.dataflow.core.node.resolver;

import ink.andromeda.dataflow.core.SourceEntity;
import ink.andromeda.dataflow.core.SpringELExpressionService;
import ink.andromeda.dataflow.core.TransferEntity;

import java.util.HashMap;
import java.util.Map;

public class SimpleCopyFieldsResolver extends DefaultConfigurationResolver{

    public SimpleCopyFieldsResolver(SpringELExpressionService expressionService) {
        super(expressionService);
    }

    @Override
    public String getName() {
        return "simple_copy_fields";
    }

    @Override
    public void resolve(SourceEntity source, TransferEntity input, TransferEntity target, Object config, Map<String, Object> rootData) throws Exception {
        if(config == null || !(boolean)config){
            return;
        }
        if(target.getData() == null)
            target.setData(new HashMap<>());
        target.getData().putAll(input.getData());
    }
}
