package ink.andromeda.dataflow.redis;

import org.springframework.stereotype.Component;

public class RedisLock {

    private final RedisClient redisClient;

    public RedisLock(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public boolean lock(String key, int maxLockedTime){
        boolean success = redisClient.incr(key) == 1L;
        if(success)
            redisClient.expire(key, maxLockedTime);
        return success;
    }

    public boolean lock(String key){
        return lock(key, 3600);
    }

    public void unlock(String key){
        redisClient.del(key);
    }
}
