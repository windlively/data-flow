package ink.windlively.dataflow.monitor.impl;

import ink.windlively.dataflow.core.DataFlowProperties;
import ink.windlively.dataflow.monitor.AppStatusProvider;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClusterAppStatusProvider implements AppStatusProvider {

    private final ThreadLocal<String> instanceName = new ThreadLocal<>();

    private final RedisTemplate<String, String> redisTemplate;

    public ClusterAppStatusProvider(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void setInstanceName(String instanceName) {
        this.instanceName.set(instanceName);
    }

    @Override
    public List<String> getActiveInstances() {
        return null;
    }

    @Override
    public List<String> getAllInstances() {
        return redisTemplate.opsForHash().keys(DataFlowProperties.REDIS_INSTANCE_REGISTER_KEY)
                .stream()
                .map(s -> (String) s).collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getReceiveMsgCount() {
        Map<String, Long> result = new HashMap<>();

        // 未提供实例名称，获取所有机器的数据
        if (instanceName.get() == null) {
            getAllInstances().forEach(
                    instance -> {
                        redisTemplate.opsForHash()
                                .entries(DataFlowProperties.REDIS_KEY_PREFIX + "monitor:" + instance + ":receive")
                                .forEach((namespace, count) -> {
                                    result.put((String) namespace, result.getOrDefault(namespace, 0L) + Long.parseLong((String) count));
                                });
                    }
            );
            return result;
        }

        String instance = instanceName.get();
        return redisTemplate.opsForHash()
                .entries(DataFlowProperties.REDIS_KEY_PREFIX + "monitor:" + instance + ":receive")
                .entrySet()
                .stream()
                .collect(Collectors.toMap(e -> (String) e.getKey(), e -> (Long) e.getValue()));

    }
}
