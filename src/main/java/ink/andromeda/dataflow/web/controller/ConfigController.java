package ink.andromeda.dataflow.web.controller;

import com.alibaba.fastjson.JSONObject;
import ink.andromeda.dataflow.entity.HttpResult;
import ink.andromeda.dataflow.service.MongoConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static ink.andromeda.dataflow.entity.HttpResult.SUCCESS;

@RestController
@RequestMapping("/config")
@Api(tags = "配置管理")
public class ConfigController {

    private final MongoConfigService configService;

    public ConfigController(MongoConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/general/{collection}")
    @ApiOperation("获取某个表的配置")
    public HttpResult<Map<String, Object>> getConfig(@PathVariable String collection,
                                                     @RequestParam String schema,
                                                     @RequestParam String table) {
        return HttpResult.SUCCESS(configService.getConfig(schema, table, collection));
    }

    @PutMapping("/general/{collection}")
    @ApiOperation("更新配置")
    public HttpResult<Number> updateConfig(@RequestBody JSONObject newConfig,
                                      @PathVariable String collection) {
        return HttpResult.SUCCESS(configService.updateConfig(collection, newConfig));
    }

    @PostMapping("/general/{collection}")
    @ApiOperation("添加配置")
    public HttpResult<Boolean> addConfig(@RequestBody JSONObject newConfig, @PathVariable("collection") String collection) {
        return HttpResult.SUCCESS(configService.newConfig(collection, newConfig));
    }

    @DeleteMapping("/general/{collection}")
    @ApiOperation("删除配置")
    public HttpResult<?> deleteConfig(@PathVariable("collection") String collection,
                                      @RequestParam String schema,
                                      @RequestParam String table){
        return HttpResult.SUCCESS(configService.deleteConfig(schema, table, collection));
    }

    @DeleteMapping("cache")
    @ApiOperation("清除所有缓存")
    public HttpResult<Integer> expireCache() {
        configService.expireCache();
        return HttpResult.SUCCESS("remove " + 1 + " cache item", 1);
    }

    @RequestMapping("validation/{collection}")
    @ApiOperation("配置校验")
    public HttpResult<List<String>> validateConfig(@RequestBody JSONObject config,
                                                   @PathVariable("collection") String collection) {
        return HttpResult.SUCCESS(configService.validateConfig(collection, config, false));
    }

    @GetMapping("template/{collection}")
    @ApiOperation("获取模板配置")
    public HttpResult<JSONObject> getTemplateConfig(@PathVariable("collection") String collection) {
        return HttpResult.SUCCESS(configService.getConfigTemplate(collection));

    }

    @GetMapping("meta-data")
    @ApiOperation("获取元数据")
    public HttpResult<Map<String, Object>> getConfigMetaData() {
        return HttpResult.SUCCESS(configService.getConfigMetaData());
    }

}
