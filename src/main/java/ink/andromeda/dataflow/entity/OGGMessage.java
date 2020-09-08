package ink.andromeda.dataflow.entity;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.Date;
import java.util.Map;

import static ink.andromeda.dataflow.util.GeneralUtils.toJSONString;

@Data
public class OGGMessage {

    private String table;

    private String opType;

    private Date opTs;

    private Date currentTs;

    private long pos;

    private String[] primaryKeys;

    private Map<String, Object> before;

    private Map<String, Object> after;

    private String simpleTableName;

    private String schemaName;

    @Override
    public String toString(){
        return toJSONString(this);
    }

}
