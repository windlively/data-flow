package ink.windlively.dataflow.monitor.impl;

import ink.windlively.dataflow.core.DataFlowProperties;
import ink.windlively.dataflow.monitor.AppStatusData;
import ink.windlively.dataflow.monitor.AppStatusProvider;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.util.*;
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
        Set<String> keys = new HashSet<>();
        try (
                RedisConnection connection = Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection();
        ) {
            ScanOptions scanOptions = ScanOptions.scanOptions()
                    .match(DataFlowProperties.REDIS_ACTIVE_INSTANCE_KEY_PREFIX + "*")
                    .count(100)
                    .build();
            Cursor<byte[]> cursor = connection.scan(scanOptions);
            while (cursor.hasNext()) keys.add(new String(cursor.next()));
            return keys.stream().map(s -> s.substring(s.lastIndexOf(":") + 1)).collect(Collectors.toList());
        }
    }

    @Override
    public List<String> getAllInstances() {
        return redisTemplate.opsForHash().keys(DataFlowProperties.REDIS_INSTANCE_REGISTER_KEY)
                .stream()
                .map(s -> (String) s).collect(Collectors.toList());
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
