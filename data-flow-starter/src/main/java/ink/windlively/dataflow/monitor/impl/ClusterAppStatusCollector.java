package ink.windlively.dataflow.monitor.impl;

import ink.windlively.dataflow.core.DataFlowProperties;
import ink.windlively.dataflow.core.FlowFailInfo;
import ink.windlively.dataflow.core.SourceEntity;
import ink.windlively.dataflow.monitor.AppStatusCollector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static ink.windlively.dataflow.monitor.StatisticFields.*;

@Slf4j
public class ClusterAppStatusCollector implements AppStatusCollector, InitializingBean {

    private final RedisTemplate<String, String> redisTemplate;

    private final String instanceName;

    private final String redisPrefix;

    public final static String MONITOR_REDIS_KEY_PREFIX = DataFlowProperties.REDIS_KEY_PREFIX + "monitor:";

    public ClusterAppStatusCollector(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            instanceName = localHost.getHostName() + "@" + localHost.getHostAddress();
            log.info("current instance name {}", instanceName);
            redisPrefix = MONITOR_REDIS_KEY_PREFIX + instanceName + ":";
            redisTemplate.opsForHash().increment(DataFlowProperties.REDIS_INSTANCE_REGISTER_KEY, instanceName, 1);
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

    private void redisHashIncrOne(String key, String hashKey){
        redisTemplate.opsForHash().increment(key, hashKey, 1);
    }

    private static String genNameSpace(SourceEntity sourceEntity){
        return String.join(".", sourceEntity.getSource(), sourceEntity.getSchema(), sourceEntity.getName());
    }
}
