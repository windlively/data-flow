package ink.windlively.dataflow.monitor.impl;

import ink.windlively.dataflow.core.DataFlowProperties;
import ink.windlively.dataflow.monitor.AppStatusData;
import ink.windlively.dataflow.monitor.AppStatusProvider;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ink.windlively.dataflow.monitor.StatisticFields.*;
import static ink.windlively.dataflow.monitor.impl.ClusterAppStatusCollector.MONITOR_REDIS_KEY_PREFIX;

public class ClusterAppStatusProvider implements AppStatusProvider {

    private final ThreadLocal<String> instanceName = new ThreadLocal<>();

    private final RedisTemplate<String, String> redisTemplate;

    public ClusterAppStatusProvider(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    List<String> monitorItems = Arrays.asList(STC_FIELD_MSG_RECEIVED, STC_FIELD_MSG_PROCESSED, STC_FIELD_INFLOW, STC_FIELD_SUCCESSFUL, STC_FIELD_FAILURE);

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
    public Map<String, Long> getMsgReceivedCount() {
        Map<String, Long> result = new HashMap<>();

        // 未提供实例名称，获取所有机器的数据
        if (instanceName.get() == null) {
            getAllInstances().forEach(instance -> mergeRedisHashMap(MONITOR_REDIS_KEY_PREFIX + instance + ":" + STC_FIELD_MSG_RECEIVED, result));
            return result;
        }

        String instance = instanceName.get();
        return redisTemplate.opsForHash()
                .entries(MONITOR_REDIS_KEY_PREFIX + instance + ":" + STC_FIELD_MSG_RECEIVED)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(e -> (String) e.getKey(), e -> (Long) e.getValue()));

    }

    @Override
    public Map<String, Long> getFlowSuccessCount() {
        Map<String, Long> result = new HashMap<>();
        if (instanceName.get() == null) {
            getAllInstances().forEach(instance -> mergeRedisHashMap(MONITOR_REDIS_KEY_PREFIX + instance + ":" + STC_FIELD_SUCCESSFUL, result));
        }
        String instance = instanceName.get();
        return redisTemplate.opsForHash()
                .entries(MONITOR_REDIS_KEY_PREFIX + instance + ":" + STC_FIELD_SUCCESSFUL)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(e -> (String) e.getKey(), e -> (Long) e.getValue()));
    }

    @Override
    public AppStatusData getAppStatusData() {

        Map<String, Map<String, Long>> statisticData = monitorItems
                .stream()
                .collect(Collectors.toMap(c -> c, c -> new HashMap<>()));

        getAllInstances().forEach(instance -> monitorItems.forEach(i -> mergeRedisHashMap(MONITOR_REDIS_KEY_PREFIX + instance + ":" + i, statisticData.get(i))));

        return AppStatusData.fromMap(statisticData);
    }

    @Override
    public Map<String, AppStatusData> getAppStatusData(List<String> instances) {
        return instances.stream().collect(Collectors.toMap(i -> i, this::getOneInstanceStatusData));
    }

    private AppStatusData getOneInstanceStatusData(String instance) {
        return AppStatusData.fromMap(
                monitorItems.stream()
                        .collect(
                                Collectors.toMap(
                                        i -> i,
                                        i -> redisTemplate.opsForHash().entries(MONITOR_REDIS_KEY_PREFIX + instance + ":" + i)
                                                .entrySet()
                                                .stream()
                                                .collect(Collectors.toMap(e -> (String) e.getKey(), e -> Long.parseLong((String) e.getValue())))
                                )
                        )
        );
    }

    private void mergeRedisHashMap(String key, Map<String, Long> map) {
        redisTemplate.opsForHash()
                .entries(key)
                .forEach((dataName, count) -> map.put((String) dataName, map.getOrDefault(dataName, 0L) + Long.parseLong((String) count)));
    }
}
