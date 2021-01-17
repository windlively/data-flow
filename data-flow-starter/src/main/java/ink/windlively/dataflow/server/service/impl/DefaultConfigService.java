package ink.windlively.dataflow.server.service.impl;

import ink.windlively.dataflow.core.flow.ConfigurableDataFlowManager;
import ink.windlively.dataflow.server.entity.RefreshCacheMessage;
import ink.windlively.dataflow.server.service.FlowConfigService;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;

import static ink.windlively.dataflow.server.entity.RefreshCacheMessage.TOPIC_NAME;

public class DefaultConfigService implements FlowConfigService {

    private final ConfigurableDataFlowManager dataFlowManager;

    private final RedisTemplate<String, String> redisTemplate;

    public DefaultConfigService(ConfigurableDataFlowManager dataFlowManager,
                                RedisTemplate<String, String> redisTemplate) {
        this.dataFlowManager = dataFlowManager;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<Map<String, Object>> getAllFlowConfig() {
        return dataFlowManager.getFlowConfig();
    }

    @Override
    public int updateFlowConfig(Map<String, Object> flowConfig) {
        return dataFlowManager.updateFlowConfig((String) flowConfig.get("_id"), flowConfig);
    }

    @Override
    public int addFlowConfig(Map<String, Object> flowConfig) {
        return dataFlowManager.addFlowConfig(flowConfig);
    }

    @Override
    public int deleteFlowConfig(String[] flowIds) {
        if (flowIds == null || flowIds.length == 0) {
            return 0;
        }
        int i = 0;
        for (String flowId : flowIds) {
            i += dataFlowManager.deleteFlowConfig(flowId);
        }
        return i;
    }

    @Override
    public int reloadFlow(String[] flowIds) {
        redisTemplate.convertAndSend(TOPIC_NAME, RefreshCacheMessage.builder()
                .cacheType("flow-config")
                .subExpression(flowIds == null || flowIds.length == 0 ? null : String.join(",", flowIds))
                .build().toString());
        return 1;
    }

    @Override
    public int reloadFlow() {
        return reloadFlow(null);
    }

    @Override
    public int reloadFlow(String source, String schema, String name) {
        return reloadFlow(
                dataFlowManager.getFlowConfig(source, schema, name)
                        .stream()
                        .map(s -> (String) s.get("_id"))
                        .toArray(String[]::new)
        );

    }
}
