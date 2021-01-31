package ink.windlively.dataflow.server.web.ws;

import ink.windlively.dataflow.server.service.FlowMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static ink.windlively.dataflow.util.GeneralTools.toJSONString;

@Component
@ServerEndpoint("/ws/monitor/status-data/{instance-name}")
@Slf4j
public class StatusDataEndpoint {

    private Session session;

    private static final Map<String, Map<String, Session>> map = new ConcurrentHashMap<>();

    @Resource
    private FlowMonitorService monitorService;

    public StatusDataEndpoint() {

    }

    @OnOpen
    public void onOpen(Session session, @PathParam("instance-name") String instanceName){
        this.session = session;
        if(StringUtils.isEmpty(instanceName)) instanceName = "__all";
        map.computeIfAbsent(instanceName, k -> new ConcurrentHashMap<>()).put(session.getId(), session);
        log.info("connected to ws server, id: {}, uri: {}", session.getId(), session.getRequestURI());
    }

    @OnClose
    public void onClose(Session session){
        removeSession(session);
        log.info("close ws connection, id: {}, uri: {}", session.getId(), session.getRequestURI());
    }

    @OnError
    public void onError(Throwable throwable, Session session){
        removeSession(session);
        log.error("exception in ws server, id: {}, uri: {}, msg: {}", session.getId(), session.getRequestURI(), throwable.getMessage(), throwable);
    }

    @PostConstruct
    public void init(){
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            if(map.isEmpty()) return;
            map.forEach((instance, clients) -> {
                if(clients == null || clients.isEmpty()) return;
                String json = toJSONString(monitorService.getStatusData(instance.equals("__all") ? null : instance));
                clients.values().forEach(c -> c.getAsyncRemote().sendText(json));
            });

        }, 0, 1, TimeUnit.SECONDS);
    }

    private static void removeSession(Session session){
        map.forEach((i, c) -> {
            c.remove(session.getId());
            if(c.isEmpty()) map.remove(i);
        });
    }

}
