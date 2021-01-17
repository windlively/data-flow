package ink.windlively.dataflow.core;

import lombok.Getter;
import org.springframework.lang.Nullable;

public class FlowNodeProcessException extends IllegalStateException {

    @Getter
    private final String nodeName;

    @Getter
    private final Throwable originalException;

    @Getter
    @Nullable
    private final String resolverName;

    public FlowNodeProcessException(String nodeName, Throwable ex){
        super("an exception occur in node: " + nodeName + ", msg: " + ex.getMessage(), ex);
        this.nodeName = nodeName;
        originalException = ex;
        resolverName = "unknown";
    }

    public FlowNodeProcessException(String nodeName, String resolverName, Throwable ex){
        super("an exception occur in node: " + nodeName + ", resolver: " + resolverName + ", msg: " + ex.getMessage(), ex);
        this.nodeName = nodeName;
        this.originalException = ex;
        this.resolverName = resolverName;
    }

}
