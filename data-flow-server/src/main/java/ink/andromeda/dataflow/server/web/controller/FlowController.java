package ink.andromeda.dataflow.server.web.controller;

import ink.andromeda.dataflow.core.DataRouter;
import ink.andromeda.dataflow.core.SourceEntity;
import ink.andromeda.dataflow.core.TransferEntity;
import ink.andromeda.dataflow.core.flow.DataFlowManager;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FlowController {


    private final DataRouter dataRouter;

    private final DataFlowManager dataFlowManager;

    public FlowController(DataRouter dataRouter,
                          DataFlowManager dataFlowManager) {
        this.dataRouter = dataRouter;
        this.dataFlowManager = dataFlowManager;
    }

    @RequestMapping("inflow")
    public List<TransferEntity> inflow(@RequestBody SourceEntity source) throws Exception {
        return dataRouter.routeAndProcess(source);
    }

    @RequestMapping("reload")
    public String reload(){
        dataFlowManager.reload();
        return "success";
    }
}
