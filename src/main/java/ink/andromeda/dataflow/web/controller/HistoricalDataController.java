package ink.andromeda.dataflow.web.controller;

import ink.andromeda.dataflow.entity.HttpResult;
import ink.andromeda.dataflow.service.HistoricalDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static ink.andromeda.dataflow.entity.HttpResult.FAILED;
import static ink.andromeda.dataflow.entity.HttpResult.SUCCESS;

@RestController
@RequestMapping("/historical")
@Api(tags = "历史数据")
public class HistoricalDataController {

    private final HistoricalDataService historicalDataService;

    public HistoricalDataController(HistoricalDataService historicalDataService) {
        this.historicalDataService = historicalDataService;
    }

    @RequestMapping(value = "re-sync/order-no/", method = {RequestMethod.PATCH, RequestMethod.POST, RequestMethod.GET})
    @ApiOperation("历史数据同步")
    public HttpResult<String> syncHistoricalData(@RequestParam String source,
                                                 @RequestParam String channelOrderNo,
                                                 @RequestParam(required = false, defaultValue = "true") boolean pushEvent,
                                                 @RequestParam(required = false, defaultValue = "all") String type) {
        return HttpResult.SUCCESS(historicalDataService.syncHistoricalData(channelOrderNo, source, pushEvent, type));
    }

    @PatchMapping("re-sync/haier/test")
    public HttpResult<?> syncHistoricalData(@RequestParam("page") int page,
                                            @RequestParam("size") int size) {
        // return SUCCESS(historicalDataService.testHaierSyncHistoricalData(page, size));
        return HttpResult.FAILED("not available interface");
    }

    @PatchMapping("re-sync/haier/all")
    @ApiOperation("海尔全量数据同步")
    public HttpResult<?> syncHaierHistoricalData(@RequestParam("size") int pageSize,
                                                 @RequestParam("key") String key,
                                                 @RequestParam(name = "limit", required = false, defaultValue = "-1") int limit) {
        return HttpResult.SUCCESS(historicalDataService.haierFullHistoricalDataSync(pageSize, key, limit));
    }

    @RequestMapping(path = "batch-re-sync", method = {RequestMethod.POST, RequestMethod.PATCH})
    @ApiOperation("批量历史数据同步")
    public HttpResult<String> syncHistoricalBatchData(@RequestBody(required = false) String body,
                                                      @RequestParam(name = "file", required = false) MultipartFile file,
                                                      @RequestParam String source,
                                                      @RequestParam(required = false, defaultValue = "true") boolean pushEvent,
                                                      @RequestParam(required = false, defaultValue = "all") String type) throws IOException {
        if(file != null)
            body = new String(file.getBytes(), StandardCharsets.UTF_8);
        if(StringUtils.isEmpty(body)){
            return HttpResult.FAILED("上传订单号为空");
        }
        Scanner scanner = new Scanner(body);
        List<String> channelOrderNos = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String str = scanner.nextLine().trim();
            if (StringUtils.isNotEmpty(str))
                channelOrderNos.add(str);
        }
        return HttpResult.SUCCESS("已请求", historicalDataService.syncHistoricalData(channelOrderNos, source, pushEvent, type));
    }

    @GetMapping("support-table")
    @ApiOperation("支持的表")
    public HttpResult<List<String>> findTables(@RequestParam String source) {
        return HttpResult.SUCCESS(historicalDataService.findSupportHistoryConfigTableNames(source));
    }
}
