package ink.andromeda.dataflow.util;

import lombok.Getter;
import org.springframework.lang.NonNull;

import java.util.Map;

/**
 * 配置校验失败异常
 */
public class ConfigValidationException extends IllegalArgumentException {

    @Getter
    private final Map<String, String> errorInfo;

    public ConfigValidationException(@NonNull Map<String, String> errorInfo){
        super(String.format("there are %d error in config", errorInfo.size()));
        this.errorInfo = errorInfo;
    }

}
