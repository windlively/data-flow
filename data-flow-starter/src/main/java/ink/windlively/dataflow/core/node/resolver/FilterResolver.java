package ink.windlively.dataflow.core.node.resolver;

import ink.windlively.dataflow.core.FilteredException;
import ink.windlively.dataflow.core.SourceEntity;
import ink.windlively.dataflow.core.SpringELExpressionService;
import ink.windlively.dataflow.core.TransferEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;

public class FilterResolver extends DefaultConfigurationResolver{

    public FilterResolver(SpringELExpressionService expressionService) {
        super(expressionService);
    }

    @Override
    public String getName() {
        return "filter";
    }

    @Override
    public void resolve(SourceEntity source, TransferEntity input, TransferEntity target, Object config, Map<String, Object> rootData) throws Exception {
        if(config == null || StringUtils.isEmpty((String) config))
            return;
        boolean bool = Objects.requireNonNull(expressionService.executeExpression((String) config, boolean.class));
        if(!bool) throw new FilteredException("filter by [" + config + "]");
    }
}
