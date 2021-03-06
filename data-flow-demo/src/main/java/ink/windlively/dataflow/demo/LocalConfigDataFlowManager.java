package ink.windlively.dataflow.demo;

import com.google.gson.reflect.TypeToken;
import ink.windlively.dataflow.core.Registry;
import ink.windlively.dataflow.core.SpringELExpressionService;
import ink.windlively.dataflow.core.flow.ConfigurableDataFlowManager;
import ink.windlively.dataflow.core.node.resolver.DefaultConfigurationResolver;
import ink.windlively.dataflow.util.GeneralTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class LocalConfigDataFlowManager extends ConfigurableDataFlowManager {

    public LocalConfigDataFlowManager(Registry<DefaultConfigurationResolver> nodeConfigResolverRegistry,
                                      SpringELExpressionService expressionService) {
        super.nodeConfigResolverRegistrySupplier = () -> nodeConfigResolverRegistry;
        super.expressionServiceSupplier = () -> expressionService;
    }

    @Value("classpath:/flow-config/**")
    private Resource[] flowConfigResources;

    @Override
    public List<Map<String, Object>> getFlowConfig() {
        return Stream.of(flowConfigResources)
                .filter(s -> Objects.nonNull(s.getFilename()))
                .filter(s -> s.getFilename().matches("^sync-config-[\\w-]+?.json$"))
                .map(flowConfigResource -> {
                    try (InputStream is = flowConfigResource.getInputStream()) {
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        int data;
                        while ((data = is.read()) != -1) os.write(data);
                        //noinspection unchecked
                        return (Map<String, Object>) GeneralTools.GSON().fromJson(os.toString(StandardCharsets.UTF_8.name()),
                                new TypeToken<Map<String, Object>>() {
                                }.getType());

                    } catch (IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getFlowConfig(String source, String schema, String name) {
        return null;
    }

    @Override
    public Map<String, Object> getFlowConfig(String flowName) {
        return null;
    }

    @Override
    public int addFlowConfig(List<Map<String, Object>> configs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int addFlowConfig(Map<String, Object> config) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int updateFlowConfig(String flowName, Map<String, Object> update) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int deleteFlowConfig(String source, String schema, String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int deleteFlowConfig(String flowName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int addNodeConfig(String flowName, Map<String, Object> nodeConfig) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int updateNodeConfig(String flowName, String nodeName, Map<String, Object> update) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int deleteNodeConfig(String flowName, String nodeName) {
        throw new UnsupportedOperationException();
    }
}
