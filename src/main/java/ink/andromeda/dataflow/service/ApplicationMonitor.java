package ink.andromeda.dataflow.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import ink.andromeda.dataflow.entity.AppEventSubject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ink.andromeda.dataflow.configuration.RedisConfiguration;
import ink.andromeda.dataflow.ws.StatusPushServer;
import ink.andromeda.dataflow.entity.SourceEntity;
import net.abakus.coresystem.redis.RedisClient;
import net.abakus.coresystem.util.TraceIdThreadPool;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static net.abakus.coresystem.util.CommonUtils.toJSONString;
import static org.springframework.util.ReflectionUtils.doWithMethods;

@Service
@Slf4j
public class ApplicationMonitor {

    @Getter
    private static String localIp;

    private final RedisClient redisClient;

    public final static String MONITOR_KEY_PREFIX = RedisConfiguration.AppKeyPrefix + "monitor:";

    public final static String MAIN_MONITOR_KEY_PREFIX = RedisConfiguration.AppKeyPrefix + "monitor-main:";

    public final ApplicationEventService applicationEventService;

    public ApplicationMonitor(RedisClient redisClient,
                              ApplicationEventService applicationEventService) {
        this.redisClient = redisClient;
        this.applicationEventService = applicationEventService;
    }


    @PostConstruct
    public void init() {
        for (CountType type : CountType.values()) {
            countMap.put(type, new ConcurrentHashMap<>());
            totalCountMap.put(type.name(), new ConcurrentHashMap<>());
            Map<String, String> map = redisClient.hgetAll(MAIN_MONITOR_KEY_PREFIX + type.name());
            if(map != null){
                map.forEach((k, v) -> totalCountMap.get(type.name()).put(k, Long.parseLong(v)));
            }
        }
        applicationEventService.subscribe(AppEventSubject.SYNC_SUCCESS, "statistics", msg -> incrCount(CountType.FIN_SYNC, ((SourceEntity) msg).getSchema() + "-" + ((SourceEntity) msg).getName()));
        applicationEventService.subscribe(AppEventSubject.EVENT_MATCHED, "statistics", msg -> incrCount(CountType.FIN_EVENT, ((SourceEntity) msg).getSchema() + "-" + ((SourceEntity) msg).getName()));
        applicationEventService.subscribe(AppEventSubject.REC_BUS_BEAN, "statistics", msg -> incrCount(CountType.REC_TOL, ((SourceEntity) msg).getSchema() + "-" + ((SourceEntity) msg).getName()));
        try {
            InetAddress addr = InetAddress.getLocalHost();
            localIp = addr.getHostAddress();
            redisClient.hset((MONITOR_KEY_PREFIX + localIp).getBytes(), "ip".getBytes(), localIp.getBytes());
            Object msgCount = SerializationUtils.deserialize(redisClient.hget((MONITOR_KEY_PREFIX + localIp).getBytes(), "msg-count".getBytes()));
            if( msgCount instanceof ConcurrentHashMap){
                //noinspection unchecked
                countMap = (Map<CountType, Map<String, AtomicLong>>) msgCount;
            }
        } catch (UnknownHostException e) {
            log.error(e.getMessage(), e);
            // e.printStackTrace();
        }

    }

    public void incrCount(CountType type, String key) {
        countMap.get(type).computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
        long val = redisClient.hincrBy(MAIN_MONITOR_KEY_PREFIX + type.name(), key, 1);
        totalCountMap.computeIfAbsent(type.name(), k -> new ConcurrentHashMap<>()).put(key, val);
    }

    public long getCount(CountType type) {
        return countMap.get(type).values().stream().mapToLong(AtomicLong::get).sum();
    }

    public long getCount(CountType type, String key) {
        return Optional.ofNullable(countMap.get(type).get(key)).orElse(new AtomicLong(0)).get();
    }

    public Map<String, Map<String, Object>> threadPoolStatus() {
        Map<String, Map<String, Object>> result = new HashMap<>();
        doWithMethods(ThreadPoolService.class, mc -> {
            try {
                TraceIdThreadPool executor = (TraceIdThreadPool) mc.invoke(null);
                String name = mc.getName();
                if (name.startsWith("get"))
                    name = name.substring(3);
                ThreadPoolExecutor tp;
                if(executor.getExecutor() instanceof ThreadPoolExecutor) {
                    tp = (ThreadPoolExecutor) executor.getExecutor();
                    Map<String, Object> poolStatus = new HashMap<>();
                    poolStatus.put("ActiveCount", tp.getActiveCount());
                    poolStatus.put("CompletedTaskCount", tp.getCompletedTaskCount());
                    poolStatus.put("CorePoolSize", tp.getCorePoolSize());
                    poolStatus.put("KeepAliveTime", tp.getKeepAliveTime(TimeUnit.SECONDS));
                    poolStatus.put("TaskCount", tp.getTaskCount());
                    poolStatus.put("LargestPoolSize", tp.getLargestPoolSize());
                    poolStatus.put("MaximumPoolSize", tp.getMaximumPoolSize());
                    result.put(name, poolStatus);
                }
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }, mf -> mf.getReturnType().equals(ExecutorService.class));
        return result;
    }

    @Getter
    private Map<CountType, Map<String, AtomicLong>> countMap = new ConcurrentHashMap<>();

    public Map<String, ?> getServerStatus() {
        Map<String, Object> result =  new HashMap<>(2);
        Map<String, Map<String, Object>> status = new HashMap<>(8);
        result.put("instances", status);
        try {
            redisClient.keys((MONITOR_KEY_PREFIX + "*").getBytes()).forEach(s -> {
                byte[] bytes = redisClient.hget(s, "ip".getBytes());
                String ip = new String(bytes, StandardCharsets.UTF_8);
                Map<String, Object> map = new HashMap<>(2);
                Object msgCount = SerializationUtils.deserialize(redisClient.hget(s, "msg-count".getBytes()));
                Object poolStatus = SerializationUtils.deserialize(redisClient.hget(s, "thread-pool-status".getBytes()));
                Object eventSubscriber = SerializationUtils.deserialize(redisClient.hget(s, "app-event-subscriber".getBytes()));
                map.put("msg-count", msgCount);
                map.put("thread-pool-status", poolStatus);
                map.put("app-event-subscriber", eventSubscriber);
                status.put(ip, map);
            });
            Map<String, Map<String, String>> main = new HashMap<>();
            result.put("main", main);
            redisClient.keys(MAIN_MONITOR_KEY_PREFIX  + "*")
                    .forEach(i -> main.put(i.substring(i.lastIndexOf(':') + 1), redisClient.hgetAll(i)));
        }catch (Exception ignored){

        }
        return result;
    }

    @Getter
    private final Map<String, Map<String, Long>> totalCountMap = new ConcurrentHashMap<>();

    @Scheduled(fixedDelay = 1000 * 2)
    private void pushToRedis() {
        try{
            redisClient.hset((MONITOR_KEY_PREFIX + localIp).getBytes(), "msg-count".getBytes(), SerializationUtils.serialize(countMap));
            redisClient.hset((MONITOR_KEY_PREFIX + localIp).getBytes(), "thread-pool-status".getBytes(), SerializationUtils.serialize(threadPoolStatus()));
            redisClient.hset((MONITOR_KEY_PREFIX + localIp).getBytes(),
                    "app-event-subscriber".getBytes(), SerializationUtils.serialize(
                            JSON.toJSON(applicationEventService.getSubscribeList().entrySet().stream()
                                    .collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().keySet())))));
            redisClient.expire((MONITOR_KEY_PREFIX + localIp).getBytes(), 3600 << 1);
        }catch (Exception ignored){
            log.error(ignored.getMessage(), ignored);
        }

    }

    // @Scheduled(fixedDelay = 1000 * 2)
    private void publish(){
        try{
            JSONObject statusResult = new JSONObject();
            statusResult.put("instances", getServerStatus());
            statusResult.put("main", totalCountMap);
            StatusPushServer.pushStatus(toJSONString(statusResult));
        }catch (Exception ignored){

        }
    }

    enum CountType {

        REC_TOL("已收到消息数"),
        FIN_SYNC("已完成同步数量"),
        FIN_EVENT("已推送事件数");

        CountType(String desc) {

        }
    }

}
