package ink.andromeda.dataflow.core;

import java.util.List;

public interface DataFlowManager {

    List<DataFlow> getFlow();

    List<DataFlow> getFlow(String source, String schema, String name);

    void reload();

    void reload(String source, String schema, String name);

    void reload(String source, String schema, String name, String flowName);

}
