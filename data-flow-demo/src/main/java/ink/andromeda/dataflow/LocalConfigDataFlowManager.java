package ink.andromeda.dataflow;

import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;
import ink.andromeda.dataflow.core.ConfigurableDataFlowManager;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ink.andromeda.dataflow.util.GeneralTools.GSON;

public class LocalConfigDataFlowManager extends ConfigurableDataFlowManager {
    @Override
    protected List<Map<String, Object>> getFlowConfig() {
        try {
            Path configDir = Paths.get(
                    LocalConfigDataFlowManager.class.getResource(
                            "/data-flow-config-example/"
                    ).toURI()
            );
            List<Map<String, Object>> config = new ArrayList<>();
            Files.walkFileTree(
                    configDir,
                    Sets.immutableEnumSet(FileVisitOption.FOLLOW_LINKS),
                    1,
                    new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return dir.endsWith(".json") || Files.isDirectory(dir) ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Map<String, Object> flowConfig =
                            GSON().fromJson(String.join("", Files.readAllLines(file)),
                    new TypeToken<Map<String, Object>>(){}.getType());
                    config.add(flowConfig);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
            return config;
        } catch (URISyntaxException | IOException e) {
            throw new IllegalStateException(e);
        }
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
