package ink.andromeda.dataflow.core;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ConfigurableDataFlowManager implements DataFlowManager {

    protected final Map<String, List<DataFlow>> flowMap = new ConcurrentHashMap<>();

    @Override
    public List<DataFlow> getFlow() {
        return null;
    }

    @Override
    public List<DataFlow> getFlow(String source, String schema, String name) {
        return null;
    }

    @Override
    public void reload() {

    }

    @Override
    public void reload(String source, String schema, String name) {

    }

    @Override
    public void reload(String source, String schema, String name, String flowName) {

    }

    /**
     * 获取所有的flow配置
     * get all flow config
     *
     * @return JSON形式的配置
     */
    protected abstract List<Map<String, Object>> getFlowConfig();

    /**
     * 获取某个namespace下的flow配置
     *
     * @param source 源名称
     * @param schema 库名称
     * @param name   表名称
     * @return JSON形式的配置
     */
    protected abstract List<Map<String, Object>> getFlowConfig(String source, String schema, String name);

    protected abstract Map<String, Object> getFlowConfig(String flowName);

    protected abstract int addFlowConfig(String source, String schema, String name, List<Map<String, Object>> configs);

    protected abstract int addFlowConfig(String flowName, Map<String, Object> config);

    protected abstract int updateFlowConfig(String flowName, Map<String, Object> update);

    protected abstract int deleteFlowConfig(String source, String schema, String name);

    protected abstract int deleteFlowConfig(String flowName);

    protected abstract int addNodeConfig(String flowName, String nodeName, Map<String, Object> nodeConfig);

    protected abstract int updateNodeConfig(String flowName, String nodeName, Map<String, Object> update);

    protected int updateNodeConfig(String flowName, int nodeIndex, Map<String, Object> update) {
        throw new UnsupportedOperationException();
    }

    protected abstract int deleteNodeConfig(String flowName, String nodeName);

    protected int deleteNodeConfig(String flowName, int nodeIndex) {
        throw new UnsupportedOperationException();
    }

    protected Map<String, Object> getNodeConfig(String flowName, String nodeName) {
        //noinspection unchecked
        return Optional.ofNullable((List<Map<String, Object>>) getFlowConfig(flowName).get("execution_chain"))
                .orElse(Collections.emptyList())
                .stream()
                .filter(m -> Objects.equals(m.get("node_name"), nodeName))
                .findFirst()
                .orElse(null);
    }

}
