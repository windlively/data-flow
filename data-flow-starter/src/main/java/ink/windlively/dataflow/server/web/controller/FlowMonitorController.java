package ink.windlively.dataflow.server.web.controller;

import ink.windlively.dataflow.monitor.AppStatusData;
import ink.windlively.dataflow.server.entity.HttpResult;
import ink.windlively.dataflow.server.service.FlowMonitorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static ink.windlively.dataflow.server.entity.HttpResult.SUCCESS;

@RestController
@RequestMapping("monitor")
public class FlowMonitorController {

    private final FlowMonitorService flowMonitorService;

    public FlowMonitorController(FlowMonitorService flowMonitorService) {
        this.flowMonitorService = flowMonitorService;
    }

    @GetMapping("cluster/full-status-data")
    public HttpResult<AppStatusData> getFullStatusData() {
        return SUCCESS(flowMonitorService.getClusterFullStatusData());
    }

    @GetMapping(value = {"status-data", "status-data/{instance-name}"})
    public HttpResult<AppStatusData> getStatusData(@PathVariable(value = "instance-name", required = false) String instanceName){
        return SUCCESS(flowMonitorService.getStatusData(instanceName));
    }

    @GetMapping("active-instance")
    public HttpResult<List<String>> getActiveInstance() {
        return SUCCESS(flowMonitorService.getActiveInstance());
    }

    @GetMapping("all-instance")
    public HttpResult<List<String>> getAllInstance() {
        return SUCCESS(flowMonitorService.getAllInstance());
    }

}
