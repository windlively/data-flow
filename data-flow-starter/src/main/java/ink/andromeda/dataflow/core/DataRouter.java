package ink.andromeda.dataflow.core;

import java.util.ArrayList;
import java.util.List;

public interface DataRouter {

    DataFlowManager getDataFlowManager();

    List<DataFlow> route(SourceEntity sourceEntity);

    default List<TransferEntity> routeAndProcess(SourceEntity sourceEntity) throws Exception{
        List<TransferEntity> transferEntities = new ArrayList<>(8);
        for (DataFlow flow : route(sourceEntity)) {
            transferEntities.add(flow.inflow(sourceEntity));
        }
        return transferEntities;
    };

}
