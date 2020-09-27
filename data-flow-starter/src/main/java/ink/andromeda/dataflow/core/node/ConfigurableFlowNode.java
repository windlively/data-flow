package ink.andromeda.dataflow.core.node;

import ink.andromeda.dataflow.core.*;
import ink.andromeda.dataflow.core.node.resolver.DefaultConfigurationResolver;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置化的转换器
 */
@Slf4j
public class ConfigurableFlowNode implements FlowNode {

    private final Registry<DefaultConfigurationResolver> convertResolverRegistry;

    private final Registry<DefaultConfigurationResolver> exportResolverRegistry;

    private final SpringELExpressionService expressionService;

    private final String name;

    @Override
    public String getName() {
        return name;
    }

    @Setter
    @Getter
    private Map<String, Object> config;

    public ConfigurableFlowNode(String name,
                                Registry<DefaultConfigurationResolver> convertResolverRegistry,
                                @Nullable Registry<DefaultConfigurationResolver> exportResolverRegistry,
                                SpringELExpressionService expressionService) {
        this.name = name;
        this.convertResolverRegistry = convertResolverRegistry;
        this.exportResolverRegistry = exportResolverRegistry;
        this.expressionService = expressionService;
    }

    @Override
    @Nullable
    public TransferEntity convert(SourceEntity sourceEntity, TransferEntity transferEntity) throws Exception {
        Map<String, Object> root = new HashMap<>(4);
        StandardEvaluationContext context = expressionService.evaluationContext();
        context.setRootObject(root);
        context.setVariable("_src", sourceEntity);
        // context.setVariable("_tsf", transferEntity);
        if(transferEntity.getData() != null)
            transferEntity.getData().forEach(context::setVariable);
        for (DefaultConfigurationResolver resolver : convertResolverRegistry.get()) {
            /*
                EnvironmentContext environmentContext = new EnvironmentContext()
                        .setVariable("sourceEntity", sourceEntity)
                        .setVariable("transferEntity", transferEntity)
                        .setVariable("root", root)
                        .setVariable("config", config.get(resolver.getName()));
                resolver.resolve(environmentContext);
             */
            resolver.resolve(sourceEntity, transferEntity, config.get(resolver.getName()), root);
        }
        return transferEntity;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int export(SourceEntity sourceEntity, TransferEntity transferEntity) throws Exception {
        int i = 0;
        if(exportResolverRegistry == null) return i;
        for (DefaultConfigurationResolver resolver : exportResolverRegistry.get()) {
            resolver.resolve(sourceEntity, transferEntity, config.get(resolver.getName()),
                    (Map<String, Object>) expressionService.evaluationContext().getRootObject().getValue());
            i ++;
        }
        return i;
    }
}
