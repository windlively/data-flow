package ink.andromeda.dataflow.logback;


import ch.qos.logback.classic.spi.IThrowableProxy;

public class WeLogUtils {
    public WeLogUtils() {
    }

    public static String throwableToString(IThrowableProxy throwableProxy) {
        if (throwableProxy == null) {
            return null;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(throwableProxy.getClassName()).append(": ").append(throwableProxy.getMessage() == null ? "null" : throwableProxy.getMessage()).append("\n");
            boolean needStackTrace = true;
            if ("java.net.SocketTimeoutException".equals(throwableProxy.getClassName())) {
                needStackTrace = false;
            }

            for (int i = 0; i < throwableProxy.getStackTraceElementProxyArray().length - throwableProxy.getCommonFrames(); ++i) {
                String STEString = throwableProxy.getStackTraceElementProxyArray()[i].getSTEAsString();
                if (!STEString.startsWith("at net.wecash") && !STEString.startsWith("at com.wecash")) {
                    if (needStackTrace && !STEString.startsWith("at org.springframework.aop") && !STEString.startsWith("at sun.reflect") && !STEString.startsWith("at java.lang.reflect") && !STEString.startsWith("at org.springframework.cglib") && !STEString.startsWith("at org.springframework.web.servlet") && !STEString.startsWith("at org.apache.catalina") && !STEString.startsWith("at org.apache.coyote") && !STEString.startsWith("at org.springframework.web") && !STEString.startsWith("at javax.servlet.http.HttpServlet")) {
                        stringBuilder.append("    ").append(STEString).append("\n");
                    }
                } else {
                    stringBuilder.append("    ").append(STEString).append("\n");
                }
            }

            if (throwableProxy.getCommonFrames() > 0) {
                stringBuilder.append("    ... ").append(throwableProxy.getCommonFrames()).append(" common frames omitted").append("\n");
            }

            if (throwableProxy.getCause() != null) {
                stringBuilder.append("Caused by: ").append(throwableToString(throwableProxy.getCause())).append("\n");
            }

            return stringBuilder.toString();
        }
    }

}
