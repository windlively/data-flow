package ink.andromeda.dataflow.core;

import ink.andromeda.dataflow.core.flow.DataFlow;
import ink.andromeda.dataflow.core.flow.DataFlowManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 默认的路由策略，根据source, schema, name匹配flow
 */
@Slf4j
public class DefaultDataRouter implements DataRouter{

    @Setter
    @Getter
    private DataFlowManager dataFlowManager;

    public DefaultDataRouter(DataFlowManager dataFlowManager) {
        this.dataFlowManager = dataFlowManager;
    }


    @Override
    public List<DataFlow> route(SourceEntity sourceEntity) {
        List<DataFlow> flowList = dataFlowManager.getFlow(
                sourceEntity.getSource(),
                sourceEntity.getSchema(),
                sourceEntity.getName());
        if(flowList.isEmpty()){
            log.info("source: {}, schema: {}, name: {} cannot be routed to at least one flow",
                    sourceEntity.getSource(), sourceEntity.getSchema(), sourceEntity.getName());
        }else {
            log.info("be routed to flow: {}", flowList.stream().map(DataFlow::getName));
        }
        return flowList;
    }
}
