package ink.andromeda.dataflow.server.service;

import ink.andromeda.dataflow.core.flow.DefaultDataFlowManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FlowConfigService {

    private final DefaultDataFlowManager dataFlowManager;

    public FlowConfigService(DefaultDataFlowManager dataFlowManager){
        this.dataFlowManager = dataFlowManager;
    }

    public List<Map<String, Object>> getAllFlowConfig(){
        return dataFlowManager.getFlowConfig();
    }

}
