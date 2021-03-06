package ink.windlively.dataflow.server.entity;

import ink.windlively.dataflow.util.GeneralTools;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
        this.javaType = GeneralTools.jdbcTypeToJavaType(dataType);
    }
}
