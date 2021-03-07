package ink.windlively.dataflow.core;

import ink.windlively.dataflow.util.GeneralTools;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SourceEntity implements Cloneable, Serializable {

    public static final String DEFAULT_FIELD_STR_VALUE = "__default";

    private long id;

    @Nullable
    private String key;

    @Builder.Default
    private String source = DEFAULT_FIELD_STR_VALUE;

    private String schema = DEFAULT_FIELD_STR_VALUE;

    private String name = DEFAULT_FIELD_STR_VALUE;

    private long timestamp;

    @Nullable
    private String opType;

    @NonNull
    private Map<String, Object> data;

    @NonNull
    private Map<String, Object> before = Collections.emptyMap();

    public void setSchema(String schema) {
        this.schema = Objects.requireNonNull(schema, "schema is null").toLowerCase();
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name is null").toLowerCase();
    }

    public void setSource(String source) {
        this.source = Objects.requireNonNull(source, "source is null");
    }

    public void setData(Map<String, Object> data) {
        this.data = Collections.unmodifiableMap(Objects.requireNonNull(data, "data is null"));
    }

    public void setBefore(@Nullable Map<String, Object> before) {
        this.before = Collections.unmodifiableMap(Optional.ofNullable(before).orElse(Collections.emptyMap()));
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public SourceEntity clone() {
        return builder()
                .source(source)
                .schema(schema)
                .name(name)
                .opType(opType)
                .timestamp(timestamp)
                .id(id)
                .data(new HashMap<>(data))
                .before(new HashMap<>(before))
                .key(key)
                .build();
    }

    public String toString(){
        return GeneralTools.toJSONString(this);
    }
}
