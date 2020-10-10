package ink.andromeda.dataflow.demo;

import ink.andromeda.dataflow.core.DataRouter;
import ink.andromeda.dataflow.core.SourceEntity;
import ink.andromeda.dataflow.core.TransferEntity;
import ink.andromeda.dataflow.core.flow.DataFlowManager;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class WebController {

    private final DataRouter dataRouter;

    private final DataFlowManager dataFlowManager;

    public WebController(DataRouter dataRouter,
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
