package ink.windlively.dataflow.server.web.controller;

import ink.windlively.dataflow.monitor.AppStatusData;
import ink.windlively.dataflow.server.entity.HttpResult;
import ink.windlively.dataflow.server.service.FlowMonitorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static ink.windlively.dataflow.server.entity.HttpResult.SUCCESS;

@RestController
@RequestMapping("monitor")
public class FlowMonitorController {

    private final FlowMonitorService flowMonitorService;

    public FlowMonitorController(FlowMonitorService flowMonitorService) {
        this.flowMonitorService = flowMonitorService;
    }

    @GetMapping("cluster/statistics")
    public HttpResult<Map<String, ?>> getClusterStatisticCount(@RequestParam List<String> item){
        return SUCCESS(flowMonitorService.getClusterStatisticCount(item));
    }

    @GetMapping("cluster/full-status-data")
    public HttpResult<AppStatusData> getFullStatusData(){
        return SUCCESS(flowMonitorService.getClusterFullStatusData());
    }


}
