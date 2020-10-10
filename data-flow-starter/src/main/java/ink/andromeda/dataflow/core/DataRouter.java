package ink.andromeda.dataflow.core;

import ink.andromeda.dataflow.core.flow.DataFlow;
import ink.andromeda.dataflow.core.flow.DataFlowManager;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;

import static ink.andromeda.dataflow.util.GeneralTools.randomId;

/**
 * 数据路由器
 * 将{@link SourceEntity}路由到各个flow执行
 */
public interface DataRouter {

    DataFlowManager getDataFlowManager();

    void setDataFlowManager(DataFlowManager dataFlowManger);

    List<DataFlow> route(SourceEntity sourceEntity);

    default List<TransferEntity> routeAndProcess(SourceEntity sourceEntity) throws Exception{
        List<TransferEntity> transferEntities = new ArrayList<>(8);
        for (DataFlow flow : route(sourceEntity)) {
            MDC.put("traceId", randomId());
            transferEntities.add(flow.inflow(sourceEntity.clone()));
            MDC.remove("traceId");
        }
        return transferEntities;
    };

}
