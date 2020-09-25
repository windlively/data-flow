package ink.andromeda.dataflow.util;

import lombok.Getter;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 配置校验失败异常
 */
public class ConfigValidationException extends IllegalArgumentException {

    @Getter
    private final Map<String, String> errorInfo;

    public ConfigValidationException(@NonNull Map<String, String> errorInfo) {
        super(String.format("there are %d error in config:\n%s",
                errorInfo.size(),
                errorInfo.entrySet()
                        .stream()
                        .map(e -> e.getKey() + ": " + e.getValue())
                        .collect(Collectors.joining("\n"))
        ));
        this.errorInfo = errorInfo;
    }

}
