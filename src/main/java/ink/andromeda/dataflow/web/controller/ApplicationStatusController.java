package ink.andromeda.dataflow.web.controller;

import ink.andromeda.dataflow.entity.HttpResult;
import ink.andromeda.dataflow.service.ApplicationEventService;
import ink.andromeda.dataflow.service.ApplicationMonitor;
import ink.andromeda.dataflow.service.LogPushService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static ink.andromeda.dataflow.entity.HttpResult.SUCCESS;

@RestController
@RequestMapping("monitor")
@Api(tags = "应用程序状态信息")
public class ApplicationStatusController {

    private final LogPushService logPushService;

    private final ApplicationMonitor applicationMonitor;

    private final ApplicationEventService applicationEventService;

    public ApplicationStatusController(LogPushService logPushService,
                                       ApplicationMonitor applicationMonitor,
                                       ApplicationEventService applicationEventService) {
        this.logPushService = logPushService;
        this.applicationMonitor = applicationMonitor;
        this.applicationEventService = applicationEventService;
    }

    @GetMapping(value = "/log")
    @ApiOperation("日志")
    public HttpResult<?> pushLogToClient(@RequestParam String id){
        return logPushService.getLog(id);
    }

    @GetMapping("/queue")
    @ApiOperation("日志队列长度")
    public HttpResult<Integer> getLogQueueSize(){
        return logPushService.getLogQueueSize();
    }

    @GetMapping("/subscriber")
    @ApiOperation("订阅列表")
    public HttpResult<Map<?, ?>> getEventSubscriberStatus(){
        return HttpResult.SUCCESS(applicationEventService.getSubscribeList());
    }

    @GetMapping("/statistics")
    @ApiOperation("状态统计")
    public HttpResult<?> getStatisticsStatus(){
        return HttpResult.SUCCESS(applicationMonitor.getServerStatus());
    }

}
