package ink.andromeda.dataflow.core.converter;

import ink.andromeda.dataflow.core.SpringELExpressionService;
import ink.andromeda.dataflow.core.SourceEntity;
import ink.andromeda.dataflow.core.TransferEntity;
import ink.andromeda.dataflow.core.converter.configuarion.SpringELConfigurationResolver;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import java.util.Map;

/**
 * 配置化的转换器
 */
@Slf4j
public class ConfigurableDataConverter implements DataConverter {

    private final SpringELExpressionService expressionService;

    private final ConfigurableDataConvertResolverRegistry resolverRegistry;

    @Setter
    @Getter
    private Map<String, Object> config;

    public ConfigurableDataConverter(ConfigurableDataConvertResolverRegistry resolverRegistry,
                                     SpringELExpressionService expressionService) {
        this.resolverRegistry = resolverRegistry;
        this.expressionService = expressionService;
    }

    @Override
    @Nullable
    public TransferEntity convert(SourceEntity sourceEntity, TransferEntity transferEntity) {
        for (SpringELConfigurationResolver resolver : resolverRegistry.getConvertConfigResolver()) {
            try {
                resolver.resolve(sourceEntity, transferEntity, config.get(resolver.getName()),
                        expressionService.evaluationContext(), expressionService.expressionParser());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return transferEntity;
    }

    @Override
    public int export(SourceEntity sourceEntity, TransferEntity transferEntity) {
        for (SpringELConfigurationResolver resolver : resolverRegistry.getExportConfigResolver()) {
            try {
                resolver.resolve(sourceEntity, transferEntity, config.get(resolver.getName()),
                        expressionService.evaluationContext(), expressionService.expressionParser());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 1;
    }
}
