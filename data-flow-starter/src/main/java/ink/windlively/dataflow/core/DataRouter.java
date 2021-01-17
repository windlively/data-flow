package ink.windlively.dataflow.core;

import ink.windlively.dataflow.core.flow.DataFlow;
import ink.windlively.dataflow.core.flow.DataFlowManager;
import ink.windlively.dataflow.monitor.AppStatusCollector;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据路由器
 * 将{@link SourceEntity}路由到各个flow执行
 */
public interface DataRouter {

    DataFlowManager getDataFlowManager();

    void setDataFlowManager(DataFlowManager dataFlowManger);

    List<DataFlow> route(SourceEntity sourceEntity);

    default AppStatusCollector getStatusCollector() {
        return AppStatusCollector.EMPTY_COLLECTOR;
    }

    default List<TransferEntity> routeAndProcess(SourceEntity sourceEntity) throws Exception{
        List<TransferEntity> transferEntities = new ArrayList<>(8);
        for (DataFlow flow : route(sourceEntity)) {
            getStatusCollector().inflowOne(flow.getName(), sourceEntity);
            transferEntities.add(flow.inflow(sourceEntity.clone()));
            getStatusCollector().successOne(flow.getName(), sourceEntity);
        }
        return transferEntities;
    };

}
