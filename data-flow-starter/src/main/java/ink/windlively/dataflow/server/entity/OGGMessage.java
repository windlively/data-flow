package ink.windlively.dataflow.server.entity;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import ink.windlively.dataflow.util.GeneralTools;
import ink.windlively.dataflow.util.gson.adapter.OggCurrentTsTypeAdapter;
import ink.windlively.dataflow.util.gson.adapter.OggOpTsTypeAdapter;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class OGGMessage {

    private String table;

    @SerializedName("op_type")
    private String opType;

    @JsonAdapter(OggOpTsTypeAdapter.class)
    @SerializedName("op_ts")
    private Date opTs;

    @JsonAdapter(OggCurrentTsTypeAdapter.class)
    @SerializedName("current_ts")
    private Date currentTs;

    private long pos;

    @SerializedName("primary_keys")
    private String[] primaryKeys;

    private Map<String, Object> before;

    private Map<String, Object> after;

    @SerializedName("simple_table_name")
    private String simpleTableName;

    @SerializedName("schema_name")
    private String schemaName;

    @Override
    public String toString(){
        return GeneralTools.toJSONString(this);
    }

}
