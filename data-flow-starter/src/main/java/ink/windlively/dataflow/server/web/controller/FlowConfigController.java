package ink.windlively.dataflow.server.web.controller;

import ink.windlively.dataflow.server.entity.HttpResult;
import ink.windlively.dataflow.server.service.FlowConfigService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static ink.windlively.dataflow.server.entity.HttpResult.SUCCESS;

@RestController
@RequestMapping("/flow-config")
@Api("配置管理")
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

    @DeleteMapping("{flowId}")
    public HttpResult<Integer> deleteFlowConfig(@PathVariable("flowId") String flowId){
        return SUCCESS(flowConfigService.deleteFlowConfig(new String[]{flowId}));
    }

    @GetMapping("reload")
    public HttpResult<Integer> reloadFlow(@RequestParam(name = "flowIds", required = false) String[] flowIds){
        return SUCCESS(flowConfigService.reloadFlow(flowIds));
    }

}
