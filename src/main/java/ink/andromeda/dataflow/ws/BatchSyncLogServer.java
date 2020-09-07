package ink.andromeda.dataflow.ws;

import ink.andromeda.dataflow.service.HistoricalDataService;
import ink.andromeda.dataflow.service.ThreadPoolService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.abakus.coresystem.redis.RedisClient;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPubSub;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 向客户端推送日志
 */
@Component
@ServerEndpoint("/ws/batch-sync/{batchNo}")
@Slf4j
public class BatchSyncLogServer {

    private Session session;

    private String keyPrefix = HistoricalDataService.BATCH_SYNC_KEY_PREFIX;

    private static final ConcurrentHashMap<String, Session> clients = new ConcurrentHashMap<>();

    private final JedisPubSub jedisPubSub = new JedisPubSub() {

        @Override
        public void onMessage(String channel, String message) {
            try {
                if (session != null)
                    session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSubscribe(String channel, int subscribedChannels) {
            log.info("ws: {} subscribe channel: {}, left subscribed channel count is: {}",
                    session.getId(), channel, subscribedChannels);
        }

        @Override
        public void onUnsubscribe(String channel, int subscribedChannels) {
            log.info("ws: {} unsubscribe channel: {}, left subscribed channel count is: {}",
                    session.getId(), channel, subscribedChannels);
        }

    };

    @Setter
    private static RedisClient redisClient;

    @OnOpen
    public void onOpen(Session session, @PathParam("batchNo") String batchNo) {
        this.session = session;
        this.keyPrefix += batchNo;
        try {
            long length = Optional.ofNullable(redisClient.llen(keyPrefix)).orElse(0L);
            long start = length > 1000 ? length - 1000 : 0;
            List<String> list = redisClient.lrange(keyPrefix, start, length - 1);
            for (String m : list) {
                session.getBasicRemote().sendText(m);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ThreadPoolService.ASYNC_TASK_GROUP().submit(() -> redisClient.subscribe(jedisPubSub, keyPrefix));
        /*
            使用redis订阅/发布功能代替，适用于分布式环境
            Stream.of(HIS_BATCH_ALL_FIN, HIS_BATCH_ONE_FIN, HIS_EVENT_FIN, HIS_SYNC_FIN).forEach(e ->
                    ApplicationEventService.subscribe(e, "batch-sync-monitor-" + batchNo, msg -> {
                        try {
                            if(batchNo.equals(Objects.requireNonNull(findMethod(msg.getClass(), "getBatchNo")).invoke(msg)))
                                session.getBasicRemote().sendText(e.name() + ":" + toJSONString(msg));
                        } catch (IOException | IllegalAccessException | InvocationTargetException ex) {
                            ex.printStackTrace();
                        }
                    }));
        */
        log.info("open web socket, id: {}, uri: {}", session.getId(), session.getRequestURI());
    }

    @OnClose
    public void onClose() {
        if (jedisPubSub.isSubscribed())
            jedisPubSub.unsubscribe(keyPrefix);
//        Stream.of(HIS_BATCH_ALL_FIN, HIS_BATCH_ONE_FIN, HIS_EVENT_FIN, HIS_SYNC_FIN)
//                .forEach(e -> ApplicationEventService.unsubscribe(e, "batch-sync-monitor-" + batchNo));
        log.info("close web socket, id: {}, uri: {}", session.getId(), session.getRequestURI());
    }

    @OnError
    public void onError(Throwable throwable, Session session) {
        if (jedisPubSub.isSubscribed())
            jedisPubSub.unsubscribe(keyPrefix);
        throwable.printStackTrace();
        log.error("error in web socket, id: {}, uri: {}, message: {}", session.getId(), session.getRequestURI(), throwable.getMessage(), throwable);
    }

}
