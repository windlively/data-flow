package ink.andromeda.dataflow.server.web.controller;

import ink.andromeda.dataflow.server.entity.HttpResult;
import ink.andromeda.dataflow.server.service.FlowConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static ink.andromeda.dataflow.server.entity.HttpResult.SUCCESS;

@RestController
@RequestMapping("/flow-config")
public class FlowConfigController {

    private final FlowConfigService flowConfigService;

    public FlowConfigController(FlowConfigService flowConfigService) {
        this.flowConfigService = flowConfigService;
    }

    @GetMapping("all")
    public HttpResult<List<Map<String, Object>>> getAllFlowConfig(){
        return SUCCESS(flowConfigService.getAllFlowConfig());
    }

}
