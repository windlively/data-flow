package ink.windlively.dataflow.monitor.impl;

import ink.windlively.dataflow.core.DataFlowProperties;
import ink.windlively.dataflow.core.FlowFailInfo;
import ink.windlively.dataflow.core.SourceEntity;
import ink.windlively.dataflow.monitor.AppStatusCollector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static ink.windlively.dataflow.monitor.StatisticFields.*;

@Slf4j
public class ClusterAppStatusCollector implements AppStatusCollector, InitializingBean {

    private final RedisTemplate<String, String> redisTemplate;

    private final String instanceName;

    private final String redisPrefix;

    public final static String MONITOR_REDIS_KEY_PREFIX = DataFlowProperties.REDIS_KEY_PREFIX + "monitor:";

    public final String redisActiveKey;

    public final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public ClusterAppStatusCollector(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            instanceName = localHost.getHostName() + "@" + localHost.getHostAddress();
            log.info("current instance name {}", instanceName);
            // 监控数据的key前缀
            redisPrefix = MONITOR_REDIS_KEY_PREFIX + instanceName + ":";
            // 当前实例活动监测的key
            redisActiveKey = MONITOR_REDIS_KEY_PREFIX + "active-instance:" + instanceName;
            // 向redis保存当前实例名称，不会删除
            redisTemplate.opsForHash().increment(DataFlowProperties.REDIS_INSTANCE_REGISTER_KEY, instanceName, 1);
            // 心跳检测
            redisTemplate.opsForValue().set(redisActiveKey, "active", 2, TimeUnit.MINUTES);
            executorService.scheduleWithFixedDelay(this::heartbeats, 0, 10, TimeUnit.SECONDS);
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }
    }


    @Override
    public void receiveOneMsg(SourceEntity sourceEntity) {
        redisHashIncrOne(redisPrefix + STC_FIELD_MSG_RECEIVED, genNameSpace(sourceEntity));
    }

    @Override
    public void processOneMsg(SourceEntity sourceEntity) {
        redisHashIncrOne(redisPrefix + STC_FIELD_MSG_PROCESSED, genNameSpace(sourceEntity));
    }

    @Override
    public void inflowOne(String flowName, SourceEntity sourceEntity) {
        redisHashIncrOne(redisPrefix + STC_FIELD_INFLOW, flowName);
    }

    @Override
    public void successOne(String flowName, SourceEntity sourceEntity) {
        redisHashIncrOne(redisPrefix + STC_FIELD_SUCCESSFUL, flowName);
    }

    @Override
    public void failedOne(String flowName, SourceEntity sourceEntity) {
        redisHashIncrOne(redisPrefix + STC_FIELD_FAILURE, flowName);
    }

    @Override
    public void failedOne(String flowName, SourceEntity sourceEntity, @Nullable FlowFailInfo failInfo) {
        failedOne(flowName, sourceEntity);
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    private void redisHashIncrOne(String key, String hashKey) {
        redisTemplate.opsForHash().increment(key, hashKey, 1);
    }

    private static String genNameSpace(SourceEntity sourceEntity) {
        return String.join(".", sourceEntity.getSource(), sourceEntity.getSchema(), sourceEntity.getName());
    }

    private void heartbeats() {
        redisTemplate.expire(redisActiveKey, 2, TimeUnit.MINUTES);
    }
}
