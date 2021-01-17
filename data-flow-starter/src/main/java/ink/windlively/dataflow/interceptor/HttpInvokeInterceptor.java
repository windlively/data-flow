package ink.windlively.dataflow.interceptor;

import ink.windlively.dataflow.exception.InterfaceInterceptedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class HttpInvokeInterceptor implements HandlerInterceptor {

    private final boolean enable;

    private final String msgOnIntercepted;

    public HttpInvokeInterceptor(boolean enable, String msgOnIntercepted) {
        this.enable = enable;
        this.msgOnIntercepted = msgOnIntercepted;
    }

    public HttpInvokeInterceptor(boolean enable){
        this(enable, "http invoke not enabled");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        if(!enable) throw new InterfaceInterceptedException(request.getMethod() + " " + request.getRequestURI(), msgOnIntercepted);
        return true;
    }
 
    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }
 
    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
    }
}
