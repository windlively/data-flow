package ink.windlively.dataflow.server.web.ws;

import ink.windlively.dataflow.monitor.AppStatusProvider;
import lombok.extern.slf4j.Slf4j;
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
@ServerEndpoint("/ws/monitor/full-status-data")
@Slf4j
public class FullStatusDataEndpoint {

    private Session session;

    private static final Map<String, Session> clients = new ConcurrentHashMap<>();

    @Resource
    private AppStatusProvider statusProvider;

    public FullStatusDataEndpoint() {

    }

    @OnOpen
    public void onOpen(Session session){
        this.session = session;
        clients.put(session.getId(), session);
        log.info("connected to ws server, id: {}, uri: {}", session.getId(), session.getRequestURI());
    }

    @OnClose
    public void onClose(){
        clients.remove(session.getId());
        log.info("close ws connection, id: {}, uri: {}", session.getId(), session.getRequestURI());
    }

    @OnError
    public void onError(Throwable throwable, Session session){
        clients.remove(session.getId());
        log.error("exception in ws server, id: {}, uri: {}, msg: {}", session.getId(), session.getRequestURI(), throwable.getMessage(), throwable);
    }

    @PostConstruct
    public void init(){
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            if(clients.isEmpty()) return;
            clients.forEach((id, session) -> session.getAsyncRemote().sendText(toJSONString(statusProvider.getAppStatusData())));

        }, 0, 1, TimeUnit.SECONDS);
    }

}
