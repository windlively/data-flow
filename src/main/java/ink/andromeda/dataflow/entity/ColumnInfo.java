package ink.andromeda.dataflow.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import static ink.andromeda.dataflow.util.GeneralUtils.jdbcTypeToJavaType;

@ToString
@Data
@NoArgsConstructor
public class ColumnInfo {

    private String tableName;

    private String columnName;

    private String dataType;

    private Class<?> javaType;

    public void setDataType(String dataType) {
        this.dataType = dataType;
        this.javaType = jdbcTypeToJavaType(dataType);
    }
}
