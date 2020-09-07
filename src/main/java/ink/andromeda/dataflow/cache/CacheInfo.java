package ink.andromeda.dataflow.cache;

import lombok.Data;

import java.util.Date;

@Data
public class CacheInfo<K> {

    private K key;

    private Date createTime;

    private Date lastUseTime;

    private int useTimes;

    /**
     * @return 当前缓存的存活时间(ms)
     */
    public long getAliveTime(){
        return System.currentTimeMillis() - createTime.getTime();
    }
}
