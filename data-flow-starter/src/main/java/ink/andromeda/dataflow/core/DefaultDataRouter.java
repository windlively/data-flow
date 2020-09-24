package ink.andromeda.dataflow.core;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class DefaultDataRouter implements DataRouter{

    @Setter
    @Getter
    private DataFlowManager dataFlowManager;

    public DefaultDataRouter(DataFlowManager dataFlowManager) {
        this.dataFlowManager = dataFlowManager;
    }


    @Override
    public List<DataFlow> route(SourceEntity sourceEntity) {
        return dataFlowManager.getFlow(
                sourceEntity.getSource(),
                sourceEntity.getSchema(),
                sourceEntity.getSchema()
                );
    }
}
