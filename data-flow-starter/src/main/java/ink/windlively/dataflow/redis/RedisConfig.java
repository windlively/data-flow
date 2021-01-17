package ink.windlively.dataflow.redis;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import ink.windlively.dataflow.util.GeneralTools;
import lombok.Data;

@Data
public class RedisConfig {

    // 主机地址
    private String host;

    // 端口号
    private int port;

    // 多长时间无响应后关闭连接
    private int timeout;

    // 最大闲置数量
    private int maxIdle;

    // 最小闲置数量
    private int minIdle;

    // 最长等待时间
    private int maxWait;

    // 最大活动数量
    private int maxActive;

    // 所使用的的数据库
    private int databaseIndex;

    // 密码
    private String password;

    // ssl
    private boolean ssl;

    @Override
    public String toString(){
        JsonElement jsonElement = GeneralTools.GSON().toJsonTree(this);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        // 隐藏密码
        jsonObject.add("password", new JsonPrimitive("<hidden>"));
        return jsonObject.toString();

    }
}
