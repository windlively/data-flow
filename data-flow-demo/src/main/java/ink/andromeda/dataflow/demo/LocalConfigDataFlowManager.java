package ink.andromeda.dataflow.demo;

import com.google.gson.reflect.TypeToken;
import ink.andromeda.dataflow.core.Registry;
import ink.andromeda.dataflow.core.SpringELExpressionService;
import ink.andromeda.dataflow.core.flow.ConfigurableDataFlowManager;
import ink.andromeda.dataflow.core.node.resolver.DefaultConfigurationResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ink.andromeda.dataflow.util.GeneralTools.GSON;

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
    protected List<Map<String, Object>> getFlowConfig() {
        return Stream.of(flowConfigResources)
                .filter(s -> Objects.nonNull(s.getFilename()))
                .filter(s -> s.getFilename().matches("^sync-config-[\\w-]+?.json$"))
                .map(flowConfigResource -> {
                    try (InputStream is = flowConfigResource.getInputStream()) {
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        int data;
                        while ((data = is.read()) != -1) os.write(data);
                        //noinspection unchecked
                        return (Map<String, Object>) GSON().fromJson(os.toString(StandardCharsets.UTF_8.name()),
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
    protected List<Map<String, Object>> getFlowConfig(String source, String schema, String name) {
        return null;
    }

    @Override
    protected Map<String, Object> getFlowConfig(String flowName) {
        return null;
    }

    @Override
    protected int addFlowConfig(List<Map<String, Object>> configs) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected int addFlowConfig(Map<String, Object> config) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected int updateFlowConfig(String flowName, Map<String, Object> update) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected int deleteFlowConfig(String source, String schema, String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected int deleteFlowConfig(String flowName) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected int addNodeConfig(String flowName, Map<String, Object> nodeConfig) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected int updateNodeConfig(String flowName, String nodeName, Map<String, Object> update) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected int deleteNodeConfig(String flowName, String nodeName) {
        throw new UnsupportedOperationException();
    }
}
