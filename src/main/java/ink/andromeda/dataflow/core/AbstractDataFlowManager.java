package ink.andromeda.dataflow.core;


import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractDataFlowManager implements DataFlowManager{

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

    protected abstract List<Map<String, Object>> getFlowConfig();

    protected abstract List<Map<String, Object>> getFlowConfig(String source, String schema, String name);

    protected abstract Map<String, Object> getFlowConfig(String source, String schema, String name, String flowName);

}
