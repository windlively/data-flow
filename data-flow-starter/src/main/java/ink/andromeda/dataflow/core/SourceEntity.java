package ink.andromeda.dataflow.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SourceEntity implements Cloneable, Serializable {

    private String source;

    private String schema;

    private String name;

    private String opType;

    private Map<String, Object> data;

    private Map<String, Object> before;

    public void setSchema(String schema){
        this.schema = schema == null ? null : schema.toLowerCase();
    }

    public void setName(String name){
        this.name = name == null ? null : name.toLowerCase();
    }

    public void setData(Map<String, Object> data) {
        this.data = Collections.unmodifiableMap(data);
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Object clone(){
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
