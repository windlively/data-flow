package ink.andromeda.dataflow.core.node;

import ink.andromeda.dataflow.core.*;
import ink.andromeda.dataflow.core.node.resolver.DefaultConfigurationResolver;
import ink.andromeda.dataflow.util.GeneralTools;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.Nullable;

import java.util.*;

import static ink.andromeda.dataflow.util.GeneralTools.copyFields;

/**
 * 配置化的转换器
 */
@Slf4j
public class ConfigurableFlowNode implements FlowNode {

    private final Registry<DefaultConfigurationResolver> nodeConfigResolverRegistry;

    private final List<DefaultConfigurationResolver> nodeConfigResolveChain = new ArrayList<>();

    private final SpringELExpressionService expressionService;

    private final String name;

    @Override
    public String getName() {
        return name;
    }

    private boolean skipIfException;

    @Getter
    private Map<String, Object> config;

    public void setConfig(Map<String, Object> config) {
        this.config = config;
        skipIfException = (boolean) this.config.getOrDefault("skip_if_exception", false);
        //noinspection unchecked
        List<String> resolveOrder = (List<String>) this.config.get("resolve_order");
        if (resolveOrder != null) {
            nodeConfigResolveChain.sort((o1, o2) -> {
                int iO1, iO2;
                return ((iO1 = resolveOrder.indexOf(o1.getName())) == -1 ? Integer.MAX_VALUE : iO1) -
                        ((iO2 = resolveOrder.indexOf(o2.getName())) == -1 ? Integer.MAX_VALUE : iO2);
            });
        }
        log.info("{}", nodeConfigResolveChain);
    }

    public ConfigurableFlowNode(String name,
                                Registry<DefaultConfigurationResolver> nodeConfigResolverRegistry,
                                SpringELExpressionService expressionService) {
        this.name = name;
        this.nodeConfigResolverRegistry = nodeConfigResolverRegistry;
        this.expressionService = expressionService;
        nodeConfigResolveChain.addAll(nodeConfigResolverRegistry.get());


    }

    @Override
    @Nullable
    public TransferEntity apply(SourceEntity source, TransferEntity input) throws Exception {
        TransferEntity target = new TransferEntity();
        copyFields(input, target);
        target.setData(new HashMap<>());
        try {

            Map<String, Object> root = new HashMap<>(4);
            StandardEvaluationContext context = expressionService.evaluationContext();
            context.setRootObject(root);
            context.setVariable("src", source);
            context.setVariable("in", input.getData());
            context.setVariable("res", target.getData());
            if (input.getData() != null) root.putAll(input.getData());
            for (DefaultConfigurationResolver resolver : nodeConfigResolveChain) {
            /*
                EnvironmentContext environmentContext = new EnvironmentContext()
                        .setVariable("sourceEntity", sourceEntity)
                        .setVariable("transferEntity", transferEntity)
                        .setVariable("root", root)
                        .setVariable("config", config.get(resolver.getName()));
                resolver.resolve(environmentContext);
             */
                resolver.resolve(source, input, target, config.get(resolver.getName()), root);
                log.debug("resolve '{}' success", resolver.getName());
            }
        } catch (Exception ex) {
            if (skipIfException) {
                log.warn("skip exception: {}", ex.getMessage(), ex);
                target.setData(input.getData());
            } else {
                throw ex;
            }
        }
        log.info("pass node: {}", getName());
        return target;
    }

}
