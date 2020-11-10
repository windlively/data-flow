package ink.andromeda.dataflow.server.web;

import ink.andromeda.dataflow.server.entity.DefaultServerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class FlowHttpInterceptor implements HandlerInterceptor {

    private final DefaultServerConfig defaultServerConfig;

    public FlowHttpInterceptor(DefaultServerConfig defaultServerConfig) {
        this.defaultServerConfig = defaultServerConfig;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, Object o) throws Exception {
        return defaultServerConfig.isEnableHttpInvoke();
    }
 
    @Override
    public void postHandle(@NonNull HttpServletRequest httpServletRequest, @NonNull HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }
 
    @Override
    public void afterCompletion(@NonNull HttpServletRequest httpServletRequest, @NonNull HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
    }
}