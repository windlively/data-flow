package ink.andromeda.dataflow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SourceEntity {

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

}
