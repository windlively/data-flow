package ink.andromeda.dataflow.core;

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

    private long id;

    @Nullable
    private String key;

    private String source;

    private String schema;

    private String name;

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
                .data(new HashMap<>(data))
                .before(new HashMap<>(before))
                .build();
    }
}
