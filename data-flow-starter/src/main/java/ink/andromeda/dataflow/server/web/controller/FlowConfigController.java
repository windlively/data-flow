package ink.andromeda.dataflow.server.web.controller;

import ink.andromeda.dataflow.server.entity.HttpResult;
import ink.andromeda.dataflow.server.service.FlowConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static ink.andromeda.dataflow.server.entity.HttpResult.SUCCESS;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping("/flow-config")
public class FlowConfigController {

    private final FlowConfigService flowConfigService;

    public FlowConfigController(FlowConfigService flowConfigService) {
        this.flowConfigService = flowConfigService;
    }

    @GetMapping("")
    public HttpResult<List<Map<String, Object>>> getAllFlowConfig(){
        return SUCCESS(flowConfigService.getAllFlowConfig());
    }

    @PutMapping("")
    public HttpResult<Integer> updateFlowConfig(@RequestBody Map<String, Object> update){
        return SUCCESS(flowConfigService.updateFlowConfig(update));
    }

    @PostMapping("")
    public HttpResult<Integer> addFlowConfig(@RequestBody Map<String, Object> insert){
        return SUCCESS(flowConfigService.addFlowConfig(insert));
    }

    @DeleteMapping("")
    public HttpResult<Integer> deleteFlowConfig(@RequestParam("flowIds") String[] flowIds){
        return SUCCESS(flowConfigService.deleteFlowConfig(flowIds));
    }

    @GetMapping("reload")
    public HttpResult<Integer> reloadFlow(@RequestParam(name = "flowIds", required = false) String[] flowIds){
        return SUCCESS(flowConfigService.reloadFlow(flowIds));
    }

}
