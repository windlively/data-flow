package ink.windlively.dataflow.redis;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Module;
import redis.clients.jedis.*;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.params.*;
import redis.clients.jedis.util.Slowlog;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class RedisClient {

    private final JedisPool jedisPool;

    public static RedisClient build(RedisConfig redisConfig){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(redisConfig.getMaxIdle());
        config.setMinIdle(redisConfig.getMinIdle());
        config.setMaxWaitMillis(redisConfig.getMaxWait());
        log.info("build redis client: {}", redisConfig);
        return new RedisClient(new JedisPool(config, redisConfig.getHost(), redisConfig.getPort(),
                redisConfig.getTimeout(),redisConfig.getPassword(), redisConfig.getDatabaseIndex(), redisConfig.isSsl()));
    }

    public RedisClient(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }


    public String ping(String message) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.ping(message);
        }
    }


    public String set(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.set(key, value);
        }
    }


    public String set(String key, String value, SetParams params) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.set(key, value, params);
        }
    }


    public String get(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }


    public Long exists(String... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(keys);
        }
    }


    public Boolean exists(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        }
    }


    public Long del(String... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.del(keys);
        }
    }


    public Long del(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.del(key);
        }
    }


    public Long unlink(String... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.unlink(keys);
        }
    }


    public Long unlink(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.unlink(key);
        }
    }


    public String type(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.type(key);
        }
    }


    public Set<String> keys(String pattern) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.keys(pattern);
        }
    }


    public String randomKey() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.randomKey();
        }
    }


    public String rename(String oldkey, String newkey) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.rename(oldkey, newkey);
        }
    }


    public Long renamenx(String oldkey, String newkey) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.renamenx(oldkey, newkey);
        }
    }


    public Long expire(String key, int seconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.expire(key, seconds);
        }
    }


    public Long expireAt(String key, long unixTime) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.expireAt(key, unixTime);
        }
    }


    public Long ttl(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.ttl(key);
        }
    }


    public Long touch(String... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.touch(keys);
        }
    }


    public Long touch(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.touch(key);
        }
    }


    public Long move(String key, int dbIndex) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.move(key, dbIndex);
        }
    }


    public String getSet(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.getSet(key, value);
        }
    }


    public List<String> mget(String... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.mget(keys);
        }
    }


    public Long setnx(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.setnx(key, value);
        }
    }


    public String setex(String key, int seconds, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.setex(key, seconds, value);
        }
    }


    public String mset(String... keysvalues) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.mset(keysvalues);
        }
    }


    public Long msetnx(String... keysvalues) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.msetnx(keysvalues);
        }
    }


    public Long decrBy(String key, long decrement) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.decrBy(key, decrement);
        }
    }


    public Long decr(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.decr(key);
        }
    }


    public Long incrBy(String key, long increment) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incrBy(key, increment);
        }
    }


    public Double incrByFloat(String key, double increment) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incrByFloat(key, increment);
        }
    }


    public Long incr(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incr(key);
        }
    }


    public Long append(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.append(key, value);
        }
    }


    public String substr(String key, int start, int end) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.substr(key, start, end);
        }
    }


    public Long hset(String key, String field, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hset(key, field, value);
        }
    }


    public Long hset(String key, Map<String, String> hash) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hset(key, hash);
        }
    }


    public String hget(String key, String field) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hget(key, field);
        }
    }


    public Long hsetnx(String key, String field, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hsetnx(key, field, value);
        }
    }


    public String hmset(String key, Map<String, String> hash) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hmset(key, hash);
        }
    }


    public List<String> hmget(String key, String... fields) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hmget(key, fields);
        }
    }


    public Long hincrBy(String key, String field, long value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hincrBy(key, field, value);
        }
    }


    public Double hincrByFloat(String key, String field, double value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hincrByFloat(key, field, value);
        }
    }


    public Boolean hexists(String key, String field) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hexists(key, field);
        }
    }


    public Long hdel(String key, String... fields) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hdel(key, fields);
        }
    }


    public Long hlen(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hlen(key);
        }
    }


    public Set<String> hkeys(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hkeys(key);
        }
    }


    public List<String> hvals(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hvals(key);
        }
    }


    public Map<String, String> hgetAll(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hgetAll(key);
        }
    }


    public Long rpush(String key, String... strings) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.rpush(key, strings);
        }
    }


    public Long lpush(String key, String... strings) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.lpush(key, strings);
        }
    }


    public Long llen(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.llen(key);
        }
    }


    public List<String> lrange(String key, long start, long stop) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.lrange(key, start, stop);
        }
    }


    public String ltrim(String key, long start, long stop) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.ltrim(key, start, stop);
        }
    }


    public String lindex(String key, long index) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.lindex(key, index);
        }
    }


    public String lset(String key, long index, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.lset(key, index, value);
        }
    }


    public Long lrem(String key, long count, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.lrem(key, count, value);
        }
    }


    public String lpop(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.lpop(key);
        }
    }


    public String rpop(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.rpop(key);
        }
    }


    public String rpoplpush(String srckey, String dstkey) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.rpoplpush(srckey, dstkey);
        }
    }


    public Long sadd(String key, String... members) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sadd(key, members);
        }
    }


    public Set<String> smembers(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.smembers(key);
        }
    }


    public Long srem(String key, String... members) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.srem(key, members);
        }
    }


    public String spop(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.spop(key);
        }
    }


    public Set<String> spop(String key, long count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.spop(key, count);
        }
    }


    public Long smove(String srckey, String dstkey, String member) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.smove(srckey, dstkey, member);
        }
    }


    public Long scard(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.scard(key);
        }
    }


    public Boolean sismember(String key, String member) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sismember(key, member);
        }
    }


    public Set<String> sinter(String... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sinter(keys);
        }
    }


    public Long sinterstore(String dstkey, String... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sinterstore(dstkey, keys);
        }
    }


    public Set<String> sunion(String... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sunion(keys);
        }
    }


    public Long sunionstore(String dstkey, String... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sunionstore(dstkey, keys);
        }
    }


    public Set<String> sdiff(String... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sdiff(keys);
        }
    }


    public Long sdiffstore(String dstkey, String... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sdiffstore(dstkey, keys);
        }
    }


    public String srandmember(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.srandmember(key);
        }
    }


    public List<String> srandmember(String key, int count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.srandmember(key, count);
        }
    }


    public Long zadd(String key, double score, String member) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zadd(key, score, member);
        }
    }


    public Long zadd(String key, double score, String member,
                     ZAddParams params) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zadd(key, score, member, params);
        }
    }


    public Long zadd(String key, Map<String, Double> scoreMembers) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zadd(key, scoreMembers);
        }
    }


    public Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zadd(key, scoreMembers, params);
        }
    }


    public Set<String> zrange(String key, long start, long stop) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrange(key, start, stop);
        }
    }


    public Long zrem(String key, String... members) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrem(key, members);
        }
    }


    public Double zincrby(String key, double increment, String member) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zincrby(key, increment, member);
        }
    }


    public Double zincrby(String key, double increment, String member, ZIncrByParams params) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zincrby(key, increment, member, params);
        }
    }


    public Long zrank(String key, String member) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrank(key, member);
        }
    }


    public Long zrevrank(String key, String member) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrevrank(key, member);
        }
    }


    public Set<String> zrevrange(String key, long start, long stop) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrevrange(key, start, stop);
        }
    }


    public Set<Tuple> zrangeWithScores(String key, long start, long stop) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrangeWithScores(key, start, stop);
        }
    }


    public Set<Tuple> zrevrangeWithScores(String key, long start, long stop) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrevrangeWithScores(key, start, stop);
        }
    }


    public Long zcard(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zcard(key);
        }
    }


    public Double zscore(String key, String member) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zscore(key, member);
        }
    }


    public String watch(String... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.watch(keys);
        }
    }


    public List<String> sort(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sort(key);
        }
    }


    public List<String> sort(String key, SortingParams sortingParameters) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sort(key, sortingParameters);
        }
    }


    public List<String> blpop(int timeout, String... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.blpop(timeout, keys);
        }
    }


    public List<String> blpop(String... args) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.blpop(args);
        }
    }


    public List<String> brpop(String... args) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.brpop(args);
        }
    }


    public Long sort(String key, SortingParams sortingParameters, String dstkey) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sort(key, sortingParameters, dstkey);
        }
    }


    public Long sort(String key, String dstkey) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sort(key, dstkey);
        }
    }


    public List<String> brpop(int timeout, String... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.brpop(timeout, keys);
        }
    }


    public Long zcount(String key, double min, double max) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zcount(key, min, max);
        }
    }


    public Long zcount(String key, String min, String max) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zcount(key, min, max);
        }
    }


    public Set<String> zrangeByScore(String key, double min, double max) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrangeByScore(key, min, max);
        }
    }


    public Set<String> zrangeByScore(String key, String min, String max) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrangeByScore(key, min, max);
        }
    }


    public Set<String> zrangeByScore(String key, double min, double max,
                                     int offset, int count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrangeByScore(key, min, max, offset, count);
        }
    }


    public Set<String> zrangeByScore(String key, String min, String max,
                                     int offset, int count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrangeByScore(key, min, max, offset, count);
        }
    }


    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrangeByScoreWithScores(key, min, max);
        }
    }


    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrangeByScoreWithScores(key, min, max);
        }
    }


    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max,
                                              int offset, int count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
        }
    }


    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max,
                                              int offset, int count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
        }
    }


    public Set<String> zrevrangeByScore(String key, double max, double min) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrevrangeByScore(key, max, min);
        }
    }


    public Set<String> zrevrangeByScore(String key, String max, String min) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrevrangeByScore(key, max, min);
        }
    }


    public Set<String> zrevrangeByScore(String key, double max, double min,
                                        int offset, int count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrevrangeByScore(key, max, min, offset, count);
        }
    }


    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrevrangeByScoreWithScores(key, max, min);
        }
    }


    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max,
                                                 double min, int offset, int count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
        }
    }


    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max,
                                                 String min, int offset, int count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
        }
    }


    public Set<String> zrevrangeByScore(String key, String max, String min,
                                        int offset, int count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrevrangeByScore(key, max, min, offset, count);
        }
    }


    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrevrangeByScoreWithScores(key, max, min);
        }
    }


    public Long zremrangeByRank(String key, long start, long stop) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zremrangeByRank(key, start, stop);
        }
    }


    public Long zremrangeByScore(String key, double min, double max) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zremrangeByScore(key, min, max);
        }
    }


    public Long zremrangeByScore(String key, String min, String max) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zremrangeByScore(key, min, max);
        }
    }


    public Long zunionstore(String dstkey, String... sets) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zunionstore(dstkey, sets);
        }
    }


    public Long zunionstore(String dstkey, ZParams params, String... sets) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zunionstore(dstkey, params, sets);
        }
    }


    public Long zinterstore(String dstkey, String... sets) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zinterstore(dstkey, sets);
        }
    }


    public Long zinterstore(String dstkey, ZParams params, String... sets) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zinterstore(dstkey, params, sets);
        }
    }


    public Long zlexcount(String key, String min, String max) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zlexcount(key, min, max);
        }
    }


    public Set<String> zrangeByLex(String key, String min, String max) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrangeByLex(key, min, max);
        }
    }


    public Set<String> zrangeByLex(String key, String min, String max,
                                   int offset, int count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrangeByLex(key, min, max, offset, count);
        }
    }


    public Set<String> zrevrangeByLex(String key, String max, String min) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrevrangeByLex(key, max, min);
        }
    }


    public Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrevrangeByLex(key, max, min, offset, count);
        }
    }


    public Long zremrangeByLex(String key, String min, String max) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zremrangeByLex(key, min, max);
        }
    }


    public Long strlen(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.strlen(key);
        }
    }


    public Long lpushx(String key, String... string) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.lpushx(key, string);
        }
    }


    public Long persist(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.persist(key);
        }
    }


    public Long rpushx(String key, String... string) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.rpushx(key, string);
        }
    }


    public String echo(String string) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.echo(string);
        }
    }


    public Long linsert(String key, ListPosition where, String pivot,
                        String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.linsert(key, where, pivot, value);
        }
    }


    public String brpoplpush(String source, String destination, int timeout) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.brpoplpush(source, destination, timeout);
        }
    }


    public Boolean setbit(String key, long offset, boolean value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.setbit(key, offset, value);
        }
    }


    public Boolean setbit(String key, long offset, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.setbit(key, offset, value);
        }
    }


    public Boolean getbit(String key, long offset) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.getbit(key, offset);
        }
    }


    public Long setrange(String key, long offset, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.setrange(key, offset, value);
        }
    }


    public String getrange(String key, long startOffset, long endOffset) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.getrange(key, startOffset, endOffset);
        }
    }


    public Long bitpos(String key, boolean value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.bitpos(key, value);
        }
    }


    public Long bitpos(String key, boolean value, BitPosParams params) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.bitpos(key, value, params);
        }
    }


    public List<String> configGet(String pattern) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.configGet(pattern);
        }
    }


    public String configSet(String parameter, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.configSet(parameter, value);
        }
    }


    public Object eval(String script, int keyCount, String... params) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.eval(script, keyCount, params);
        }
    }


    public void subscribe(JedisPubSub jedisPubSub, String... channels) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.subscribe(jedisPubSub, channels);
        }
    }


    public Long publish(String channel, String message) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.publish(channel, message);
        }
    }


    public void psubscribe(JedisPubSub jedisPubSub, String... patterns) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.psubscribe(jedisPubSub, patterns);
        }
    }


    public Object eval(String script, List<String> keys, List<String> args) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.eval(script, keys, args);
        }
    }


    public Object eval(String script) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.eval(script);
        }
    }


    public Object evalsha(String sha1) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.evalsha(sha1);
        }
    }


    public Object evalsha(String sha1, List<String> keys, List<String> args) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.evalsha(sha1, keys, args);
        }
    }


    public Object evalsha(String sha1, int keyCount, String... params) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.evalsha(sha1, keyCount, params);
        }
    }


    public Boolean scriptExists(String sha1) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.scriptExists(sha1);
        }
    }


    public List<Boolean> scriptExists(String... sha1) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.scriptExists(sha1);
        }
    }


    public String scriptLoad(String script) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.scriptLoad(script);
        }
    }


    public List<Slowlog> slowlogGet() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.slowlogGet();
        }
    }


    public List<Slowlog> slowlogGet(long entries) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.slowlogGet(entries);
        }
    }


    public Long objectRefcount(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.objectRefcount(key);
        }
    }


    public String objectEncoding(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.objectEncoding(key);
        }
    }


    public Long objectIdletime(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.objectIdletime(key);
        }
    }


    public Long bitcount(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.bitcount(key);
        }
    }


    public Long bitcount(String key, long start, long end) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.bitcount(key, start, end);
        }
    }


    public Long bitop(BitOP op, String destKey, String... srcKeys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.bitop(op, destKey, srcKeys);
        }
    }


    public List<Map<String, String>> sentinelMasters() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sentinelMasters();
        }
    }


    public List<String> sentinelGetMasterAddrByName(String masterName) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sentinelGetMasterAddrByName(masterName);
        }
    }


    public Long sentinelReset(String pattern) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sentinelReset(pattern);
        }
    }


    public List<Map<String, String>> sentinelSlaves(String masterName) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sentinelSlaves(masterName);
        }
    }


    public String sentinelFailover(String masterName) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sentinelFailover(masterName);
        }
    }


    public String sentinelMonitor(String masterName, String ip, int port, int quorum) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sentinelMonitor(masterName, ip, port, quorum);
        }
    }


    public String sentinelRemove(String masterName) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sentinelRemove(masterName);
        }
    }


    public String sentinelSet(String masterName, Map<String, String> parameterMap) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sentinelSet(masterName, parameterMap);
        }
    }


    public byte[] dump(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.dump(key);
        }
    }


    public String restore(String key, int ttl, byte[] serializedValue) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.restore(key, ttl, serializedValue);
        }
    }


    public String restoreReplace(String key, int ttl, byte[] serializedValue) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.restoreReplace(key, ttl, serializedValue);
        }
    }


    public Long pexpire(String key, long milliseconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pexpire(key, milliseconds);
        }
    }


    public Long pexpireAt(String key, long millisecondsTimestamp) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pexpireAt(key, millisecondsTimestamp);
        }
    }


    public Long pttl(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pttl(key);
        }
    }


    public String psetex(String key, long milliseconds, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.psetex(key, milliseconds, value);
        }
    }


    public String clientKill(String ipPort) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clientKill(ipPort);
        }
    }


    public String clientGetname() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clientGetname();
        }
    }


    public String clientList() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clientList();
        }
    }


    public String clientSetname(String name) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clientSetname(name);
        }
    }


    public String migrate(String host, int port, String key,
                          int destinationDb, int timeout) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.migrate(host, port, key, destinationDb, timeout);
        }
    }


    public String migrate(String host, int port, int destinationDB,
                          int timeout, MigrateParams params, String... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.migrate(host, port, destinationDB, timeout, params, keys);
        }
    }


    public ScanResult<String> scan(String cursor) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.scan(cursor);
        }
    }


    public ScanResult<String> scan(String cursor, ScanParams params) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.scan(cursor, params);
        }
    }


    public ScanResult<Map.Entry<String, String>> hscan(String key, String cursor) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hscan(key, cursor);
        }
    }


    public ScanResult<Map.Entry<String, String>> hscan(String key, String cursor,
                                                       ScanParams params) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hscan(key, cursor, params);
        }
    }


    public ScanResult<String> sscan(String key, String cursor) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sscan(key, cursor);
        }
    }


    public ScanResult<String> sscan(String key, String cursor, ScanParams params) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sscan(key, cursor, params);
        }
    }


    public ScanResult<Tuple> zscan(String key, String cursor) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zscan(key, cursor);
        }
    }


    public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zscan(key, cursor, params);
        }
    }


    public String clusterNodes() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clusterNodes();
        }
    }


    public String readonly() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.readonly();
        }
    }


    public String clusterMeet(String ip, int port) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clusterMeet(ip, port);
        }
    }


    public String clusterReset(ClusterReset resetType) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clusterReset(resetType);
        }
    }


    public String clusterAddSlots(int... slots) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clusterAddSlots(slots);
        }
    }


    public String clusterDelSlots(int... slots) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clusterDelSlots(slots);
        }
    }


    public String clusterInfo() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clusterInfo();
        }
    }


    public List<String> clusterGetKeysInSlot(int slot, int count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clusterGetKeysInSlot(slot, count);
        }
    }


    public String clusterSetSlotNode(int slot, String nodeId) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clusterSetSlotNode(slot, nodeId);
        }
    }


    public String clusterSetSlotMigrating(int slot, String nodeId) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clusterSetSlotMigrating(slot, nodeId);
        }
    }


    public String clusterSetSlotImporting(int slot, String nodeId) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clusterSetSlotImporting(slot, nodeId);
        }
    }


    public String clusterSetSlotStable(int slot) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clusterSetSlotStable(slot);
        }
    }


    public String clusterForget(String nodeId) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clusterForget(nodeId);
        }
    }


    public String clusterFlushSlots() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clusterFlushSlots();
        }
    }


    public Long clusterKeySlot(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clusterKeySlot(key);
        }
    }


    public Long clusterCountKeysInSlot(int slot) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clusterCountKeysInSlot(slot);
        }
    }


    public String clusterSaveConfig() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clusterSaveConfig();
        }
    }


    public String clusterReplicate(String nodeId) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clusterReplicate(nodeId);
        }
    }


    public List<String> clusterSlaves(String nodeId) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clusterSlaves(nodeId);
        }
    }


    public String clusterFailover() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clusterFailover();
        }
    }


    public List<Object> clusterSlots() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clusterSlots();
        }
    }


    public String asking() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.asking();
        }
    }


    public List<String> pubsubChannels(String pattern) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pubsubChannels(pattern);
        }
    }


    public Long pubsubNumPat() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pubsubNumPat();
        }
    }


    public Map<String, String> pubsubNumSub(String... channels) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pubsubNumSub(channels);
        }
    }


    public void close() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.close();
        }
    }


    public void setDataSource(JedisPoolAbstract jedisPool) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setDataSource(jedisPool);
        }
    }


    public Long pfadd(String key, String... elements) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pfadd(key, elements);
        }
    }


    public long pfcount(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pfcount(key);
        }
    }


    public long pfcount(String... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pfcount(keys);
        }
    }


    public String pfmerge(String destkey, String... sourcekeys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pfmerge(destkey, sourcekeys);
        }
    }


    public List<String> blpop(int timeout, String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.blpop(timeout, key);
        }
    }


    public List<String> brpop(int timeout, String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.brpop(timeout, key);
        }
    }


    public Long geoadd(String key, double longitude, double latitude, String member) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.geoadd(key, longitude, latitude, member);
        }
    }


    public Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.geoadd(key, memberCoordinateMap);
        }
    }


    public Double geodist(String key, String member1, String member2) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.geodist(key, member1, member2);
        }
    }


    public Double geodist(String key, String member1, String member2, GeoUnit unit) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.geodist(key, member1, member2, unit);
        }
    }


    public List<String> geohash(String key, String... members) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.geohash(key, members);
        }
    }


    public List<GeoCoordinate> geopos(String key, String... members) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.geopos(key, members);
        }
    }


    public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude,
                                             double radius, GeoUnit unit) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.georadius(key, longitude, latitude, radius, unit);
        }
    }


    public List<GeoRadiusResponse> georadiusReadonly(String key, double longitude, double latitude,
                                                     double radius, GeoUnit unit) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.georadiusReadonly(key, longitude, latitude, radius, unit);
        }
    }


    public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude,
                                             double radius, GeoUnit unit, GeoRadiusParam param) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.georadius(key, longitude, latitude, radius, unit, param);
        }
    }


    public List<GeoRadiusResponse> georadiusReadonly(String key, double longitude, double latitude,
                                                     double radius, GeoUnit unit, GeoRadiusParam param) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.georadiusReadonly(key, longitude, latitude, radius, unit, param);
        }
    }


    public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius,
                                                     GeoUnit unit) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.georadiusByMember(key, member, radius, unit);
        }
    }


    public List<GeoRadiusResponse> georadiusByMemberReadonly(String key, String member, double radius,
                                                             GeoUnit unit) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.georadiusByMemberReadonly(key, member, radius, unit);
        }
    }


    public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius,
                                                     GeoUnit unit, GeoRadiusParam param) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.georadiusByMember(key, member, radius, unit, param);
        }
    }


    public List<GeoRadiusResponse> georadiusByMemberReadonly(String key, String member, double radius,
                                                             GeoUnit unit, GeoRadiusParam param) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.georadiusByMemberReadonly(key, member, radius, unit, param);
        }
    }


    public String moduleLoad(String path) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.moduleLoad(path);
        }
    }


    public String moduleUnload(String name) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.moduleUnload(name);
        }
    }


    public List<Module> moduleList() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.moduleList();
        }
    }


    public List<Long> bitfield(String key, String... arguments) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.bitfield(key, arguments);
        }
    }


    public Long hstrlen(String key, String field) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hstrlen(key, field);
        }
    }


    public String memoryDoctor() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.memoryDoctor();
        }
    }


    public StreamEntryID xadd(String key, StreamEntryID id, Map<String, String> hash) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.xadd(key, id, hash);
        }
    }


    public StreamEntryID xadd(String key, StreamEntryID id, Map<String, String> hash, long maxLen, boolean approximateLength) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.xadd(key, id, hash, maxLen, approximateLength);
        }
    }


    public Long xlen(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.xlen(key);
        }
    }


    public List<StreamEntry> xrange(String key, StreamEntryID start, StreamEntryID end, int count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.xrange(key, start, end, count);
        }
    }


    public List<StreamEntry> xrevrange(String key, StreamEntryID end, StreamEntryID start, int count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.xrevrange(key, end, start, count);
        }
    }


    public List<Map.Entry<String, List<StreamEntry>>> xread(int count, long block, Map.Entry<String, StreamEntryID>... streams) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.xread(count, block, streams);
        }
    }


    public long xack(String key, String group, StreamEntryID... ids) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.xack(key, group, ids);
        }
    }


    public String xgroupCreate(String key, String groupname, StreamEntryID id, boolean makeStream) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.xgroupCreate(key, groupname, id, makeStream);
        }
    }


    public String xgroupSetID(String key, String groupname, StreamEntryID id) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.xgroupSetID(key, groupname, id);
        }
    }


    public long xgroupDestroy(String key, String groupname) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.xgroupDestroy(key, groupname);
        }
    }


    public String xgroupDelConsumer(String key, String groupname, String consumerName) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.xgroupDelConsumer(key, groupname, consumerName);
        }
    }


    public long xdel(String key, StreamEntryID... ids) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.xdel(key, ids);
        }
    }


    public long xtrim(String key, long maxLen, boolean approximateLength) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.xtrim(key, maxLen, approximateLength);
        }
    }


    public List<Map.Entry<String, List<StreamEntry>>> xreadGroup(String groupname, String consumer, int count, long block,
                                                                 boolean noAck, Map.Entry<String, StreamEntryID>... streams) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.xreadGroup(groupname, consumer, count, block, noAck, streams);
        }
    }


    public List<StreamPendingEntry> xpending(String key, String groupname, StreamEntryID start, StreamEntryID end,
                                             int count, String consumername) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.xpending(key, groupname, start, end, count, consumername);
        }
    }


    public List<StreamEntry> xclaim(String key, String group, String consumername, long minIdleTime, long newIdleTime,
                                    int retries, boolean force, StreamEntryID... ids) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.xclaim(key, group, consumername, minIdleTime, newIdleTime, retries, force, ids);
        }
    }


    public Object sendCommand(ProtocolCommand cmd, String... args) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sendCommand(cmd, args);
        }
    }

    public String ping() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.ping();
        }
    }


    public byte[] ping(final byte[] message) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.ping(message);
        }
    }


    public String set(final byte[] key, final byte[] value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.set(key, value);
        }
    }


    public String set(final byte[] key, final byte[] value, final SetParams params) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.set(key, value, params);
        }
    }


    public byte[] get(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }


    public String quit() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.quit();
        }
    }


    public Long exists(final byte[]... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(keys);
        }
    }


    public Boolean exists(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        }
    }


    public Long del(final byte[]... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.del(keys);
        }
    }


    public Long del(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.del(key);
        }
    }


    public Long unlink(final byte[]... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.unlink(keys);
        }
    }


    public Long unlink(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.unlink(key);
        }
    }


    public String type(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.type(key);
        }
    }


    public String flushDB() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.flushDB();
        }
    }


    public Set<byte[]> keys(final byte[] pattern) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.keys(pattern);
        }
    }


    public byte[] randomBinaryKey() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.randomBinaryKey();
        }
    }


    public String rename(final byte[] oldkey, final byte[] newkey) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.rename(oldkey, newkey);
        }
    }


    public Long renamenx(final byte[] oldkey, final byte[] newkey) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.renamenx(oldkey, newkey);
        }
    }


    public Long dbSize() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.dbSize();
        }
    }


    public Long expire(final byte[] key, final int seconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.expire(key, seconds);
        }
    }


    public Long expireAt(final byte[] key, final long unixTime) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.expireAt(key, unixTime);
        }
    }


    public Long ttl(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.ttl(key);
        }
    }


    public Long touch(final byte[]... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.touch(keys);
        }
    }


    public Long touch(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.touch(key);
        }
    }


    public String select(final int index) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.select(index);
        }
    }


    public String swapDB(final int index1, final int index2) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.swapDB(index1, index2);
        }
    }


    public Long move(final byte[] key, final int dbIndex) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.move(key, dbIndex);
        }
    }


    public String flushAll() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.flushAll();
        }
    }


    public byte[] getSet(final byte[] key, final byte[] value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.getSet(key, value);
        }
    }


    public List<byte[]> mget(final byte[]... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.mget(keys);
        }
    }


    public Long setnx(final byte[] key, final byte[] value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.setnx(key, value);
        }
    }


    public String setex(final byte[] key, final int seconds, final byte[] value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.setex(key, seconds, value);
        }
    }


    public String mset(final byte[]... keysvalues) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.mset(keysvalues);
        }
    }


    public Long msetnx(final byte[]... keysvalues) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.msetnx(keysvalues);
        }
    }


    public Long decrBy(final byte[] key, final long decrement) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.decrBy(key, decrement);
        }
    }


    public Long decr(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.decr(key);
        }
    }


    public Long incrBy(final byte[] key, final long increment) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incrBy(key, increment);
        }
    }


    public Double incrByFloat(final byte[] key, final double increment) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incrByFloat(key, increment);
        }
    }


    public Long incr(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incr(key);
        }
    }


    public Long append(final byte[] key, final byte[] value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.append(key, value);
        }
    }


    public byte[] substr(final byte[] key, final int start, final int end) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.substr(key, start, end);
        }
    }


    public Long hset(final byte[] key, final byte[] field, final byte[] value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hset(key, field, value);
        }
    }


    public Long hset(final byte[] key, final Map<byte[], byte[]> hash) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hset(key, hash);
        }
    }


    public byte[] hget(final byte[] key, final byte[] field) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hget(key, field);
        }
    }


    public Long hsetnx(final byte[] key, final byte[] field, final byte[] value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hsetnx(key, field, value);
        }
    }


    public String hmset(final byte[] key, final Map<byte[], byte[]> hash) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hmset(key, hash);
        }
    }


    public List<byte[]> hmget(final byte[] key, final byte[]... fields) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hmget(key, fields);
        }
    }


    public Long hincrBy(final byte[] key, final byte[] field, final long value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hincrBy(key, field, value);
        }
    }


    public Double hincrByFloat(final byte[] key, final byte[] field, final double value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hincrByFloat(key, field, value);
        }
    }


    public Boolean hexists(final byte[] key, final byte[] field) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hexists(key, field);
        }
    }


    public Long hdel(final byte[] key, final byte[]... fields) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hdel(key, fields);
        }
    }


    public Long hlen(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hlen(key);
        }
    }


    public Set<byte[]> hkeys(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hkeys(key);
        }
    }


    public List<byte[]> hvals(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hvals(key);
        }
    }


    public Map<byte[], byte[]> hgetAll(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hgetAll(key);
        }
    }


    public Long rpush(final byte[] key, final byte[]... strings) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.rpush(key, strings);
        }
    }


    public Long lpush(final byte[] key, final byte[]... strings) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.lpush(key, strings);
        }
    }


    public Long llen(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.llen(key);
        }
    }


    public List<byte[]> lrange(final byte[] key, final long start, final long stop) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.lrange(key, start, stop);
        }
    }


    public String ltrim(final byte[] key, final long start, final long stop) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.ltrim(key, start, stop);
        }
    }


    public byte[] lindex(final byte[] key, final long index) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.lindex(key, index);
        }
    }


    public String lset(final byte[] key, final long index, final byte[] value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.lset(key, index, value);
        }
    }


    public Long lrem(final byte[] key, final long count, final byte[] value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.lrem(key, count, value);
        }
    }


    public byte[] lpop(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.lpop(key);
        }
    }


    public byte[] rpop(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.rpop(key);
        }
    }


    public byte[] rpoplpush(final byte[] srckey, final byte[] dstkey) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.rpoplpush(srckey, dstkey);
        }
    }


    public Long sadd(final byte[] key, final byte[]... members) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sadd(key, members);
        }
    }


    public Set<byte[]> smembers(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.smembers(key);
        }
    }


    public Long srem(final byte[] key, final byte[]... member) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.srem(key, member);
        }
    }


    public byte[] spop(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.spop(key);
        }
    }


    public Set<byte[]> spop(final byte[] key, final long count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.spop(key, count);
        }
    }


    public Long smove(final byte[] srckey, final byte[] dstkey, final byte[] member) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.smove(srckey, dstkey, member);
        }
    }


    public Long scard(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.scard(key);
        }
    }


    public Boolean sismember(final byte[] key, final byte[] member) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sismember(key, member);
        }
    }


    public Set<byte[]> sinter(final byte[]... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sinter(keys);
        }
    }


    public Long sinterstore(final byte[] dstkey, final byte[]... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sinterstore(dstkey, keys);
        }
    }


    public Set<byte[]> sunion(final byte[]... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sunion(keys);
        }
    }


    public Long sunionstore(final byte[] dstkey, final byte[]... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sunionstore(dstkey, keys);
        }
    }


    public Set<byte[]> sdiff(final byte[]... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sdiff(keys);
        }
    }


    public Long sdiffstore(final byte[] dstkey, final byte[]... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sdiffstore(dstkey, keys);
        }
    }


    public byte[] srandmember(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.srandmember(key);
        }
    }


    public List<byte[]> srandmember(final byte[] key, final int count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.srandmember(key, count);
        }
    }


    public Long zadd(final byte[] key, final double score, final byte[] member) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zadd(key, score, member);
        }
    }


    public Long zadd(final byte[] key, final double score, final byte[] member, final ZAddParams params) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zadd(key, score, member, params);
        }
    }


    public Long zadd(final byte[] key, final Map<byte[], Double> scoreMembers) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zadd(key, scoreMembers);
        }
    }


    public Long zadd(final byte[] key, final Map<byte[], Double> scoreMembers, final ZAddParams params) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zadd(key, scoreMembers, params);
        }
    }


    public Set<byte[]> zrange(final byte[] key, final long start, final long stop) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrange(key, start, stop);
        }
    }


    public Long zrem(final byte[] key, final byte[]... members) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrem(key, members);
        }
    }


    public Double zincrby(final byte[] key, final double increment, final byte[] member) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zincrby(key, increment, member);
        }
    }


    public Double zincrby(final byte[] key, final double increment, final byte[] member, final ZIncrByParams params) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zincrby(key, increment, member, params);
        }
    }


    public Long zrank(final byte[] key, final byte[] member) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrank(key, member);
        }
    }


    public Long zrevrank(final byte[] key, final byte[] member) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrevrank(key, member);
        }
    }


    public Set<byte[]> zrevrange(final byte[] key, final long start, final long stop) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrevrange(key, start, stop);
        }
    }


    public Set<Tuple> zrangeWithScores(final byte[] key, final long start, final long stop) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrangeWithScores(key, start, stop);
        }
    }


    public Set<Tuple> zrevrangeWithScores(final byte[] key, final long start, final long stop) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrevrangeWithScores(key, start, stop);
        }
    }


    public Long zcard(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zcard(key);
        }
    }


    public Double zscore(final byte[] key, final byte[] member) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zscore(key, member);
        }
    }


    public Transaction multi() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.multi();
        }
    }


    public String watch(final byte[]... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.watch(keys);
        }
    }


    public String unwatch() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.unwatch();
        }
    }


    public List<byte[]> sort(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sort(key);
        }
    }


    public List<byte[]> sort(final byte[] key, final SortingParams sortingParameters) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sort(key, sortingParameters);
        }
    }


    public List<byte[]> blpop(final int timeout, final byte[]... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.blpop(timeout, keys);
        }
    }


    public Long sort(final byte[] key, final SortingParams sortingParameters, final byte[] dstkey) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sort(key, sortingParameters, dstkey);
        }
    }


    public Long sort(final byte[] key, final byte[] dstkey) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sort(key, dstkey);
        }
    }


    public List<byte[]> brpop(final int timeout, final byte[]... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.brpop(timeout, keys);
        }
    }


    public List<byte[]> blpop(final byte[]... args) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.blpop(args);
        }
    }


    public List<byte[]> brpop(final byte[]... args) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.brpop(args);
        }
    }


    public String auth(final String password) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.auth(password);
        }
    }


    public Pipeline pipelined() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pipelined();
        }
    }


    public Long zcount(final byte[] key, final double min, final double max) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zcount(key, min, max);
        }
    }


    public Long zcount(final byte[] key, final byte[] min, final byte[] max) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zcount(key, min, max);
        }
    }


    public Set<byte[]> zrangeByScore(final byte[] key, final double min, final double max) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrangeByScore(key, min, max);
        }
    }


    public Set<byte[]> zrangeByScore(final byte[] key, final byte[] min, final byte[] max) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrangeByScore(key, min, max);
        }
    }


    public Set<byte[]> zrangeByScore(final byte[] key, final double min, final double max,
                                     final int offset, final int count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrangeByScore(key, min, max, offset, count);
        }
    }


    public Set<byte[]> zrangeByScore(final byte[] key, final byte[] min, final byte[] max,
                                     final int offset, final int count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrangeByScore(key, min, max, offset, count);
        }
    }


    public Set<Tuple> zrangeByScoreWithScores(final byte[] key, final double min, final double max) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrangeByScoreWithScores(key, min, max);
        }
    }


    public Set<Tuple> zrangeByScoreWithScores(final byte[] key, final byte[] min, final byte[] max) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrangeByScoreWithScores(key, min, max);
        }
    }


    public Set<Tuple> zrangeByScoreWithScores(final byte[] key, final double min, final double max,
                                              final int offset, final int count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
        }
    }


    public Set<Tuple> zrangeByScoreWithScores(final byte[] key, final byte[] min, final byte[] max,
                                              final int offset, final int count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
        }
    }


    public Set<byte[]> zrevrangeByScore(final byte[] key, final double max, final double min) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrevrangeByScore(key, max, min);
        }
    }


    public Set<byte[]> zrevrangeByScore(final byte[] key, final byte[] max, final byte[] min) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrevrangeByScore(key, max, min);
        }
    }


    public Set<byte[]> zrevrangeByScore(final byte[] key, final double max, final double min,
                                        final int offset, final int count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrevrangeByScore(key, max, min, offset, count);
        }
    }


    public Set<byte[]> zrevrangeByScore(final byte[] key, final byte[] max, final byte[] min,
                                        final int offset, final int count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrevrangeByScore(key, max, min, offset, count);
        }
    }


    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key, final double max, final double min) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrevrangeByScoreWithScores(key, max, min);
        }
    }


    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key, final double max,
                                                 final double min, final int offset, final int count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
        }
    }


    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key, final byte[] max, final byte[] min) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrevrangeByScoreWithScores(key, max, min);
        }
    }


    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key, final byte[] max,
                                                 final byte[] min, final int offset, final int count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
        }
    }


    public Long zremrangeByRank(final byte[] key, final long start, final long stop) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zremrangeByRank(key, start, stop);
        }
    }


    public Long zremrangeByScore(final byte[] key, final double min, final double max) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zremrangeByScore(key, min, max);
        }
    }


    public Long zremrangeByScore(final byte[] key, final byte[] min, final byte[] max) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zremrangeByScore(key, min, max);
        }
    }


    public Long zunionstore(final byte[] dstkey, final byte[]... sets) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zunionstore(dstkey, sets);
        }
    }


    public Long zunionstore(final byte[] dstkey, final ZParams params, final byte[]... sets) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zunionstore(dstkey, params, sets);
        }
    }


    public Long zinterstore(final byte[] dstkey, final byte[]... sets) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zinterstore(dstkey, sets);
        }
    }


    public Long zinterstore(final byte[] dstkey, final ZParams params, final byte[]... sets) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zinterstore(dstkey, params, sets);
        }
    }


    public Long zlexcount(final byte[] key, final byte[] min, final byte[] max) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zlexcount(key, min, max);
        }
    }


    public Set<byte[]> zrangeByLex(final byte[] key, final byte[] min, final byte[] max) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrangeByLex(key, min, max);
        }
    }


    public Set<byte[]> zrangeByLex(final byte[] key, final byte[] min, final byte[] max,
                                   final int offset, final int count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrangeByLex(key, min, max, offset, count);
        }
    }


    public Set<byte[]> zrevrangeByLex(final byte[] key, final byte[] max, final byte[] min) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrevrangeByLex(key, max, min);
        }
    }


    public Set<byte[]> zrevrangeByLex(final byte[] key, final byte[] max, final byte[] min, final int offset, final int count) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrevrangeByLex(key, max, min, offset, count);
        }
    }


    public Long zremrangeByLex(final byte[] key, final byte[] min, final byte[] max) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zremrangeByLex(key, min, max);
        }
    }


    public String save() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.save();
        }
    }


    public String bgsave() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.bgsave();
        }
    }


    public String bgrewriteaof() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.bgrewriteaof();
        }
    }


    public Long lastsave() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.lastsave();
        }
    }


    public String shutdown() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.shutdown();
        }
    }


    public String info() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.info();
        }
    }


    public String info(final String section) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.info(section);
        }
    }


    public void monitor(final JedisMonitor jedisMonitor) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.monitor(jedisMonitor);
        }
    }


    public String slaveof(final String host, final int port) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.slaveof(host, port);
        }
    }


    public String slaveofNoOne() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.slaveofNoOne();
        }
    }


    public List<byte[]> configGet(final byte[] pattern) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.configGet(pattern);
        }
    }


    public String configResetStat() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.configResetStat();
        }
    }


    public String configRewrite() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.configRewrite();
        }
    }


    public byte[] configSet(final byte[] parameter, final byte[] value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.configSet(parameter, value);
        }
    }


    public boolean isConnected() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.isConnected();
        }
    }


    public Long strlen(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.strlen(key);
        }
    }


    public void sync() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.sync();
        }
    }


    public Long lpushx(final byte[] key, final byte[]... string) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.lpushx(key, string);
        }
    }


    public Long persist(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.persist(key);
        }
    }


    public Long rpushx(final byte[] key, final byte[]... string) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.rpushx(key, string);
        }
    }


    public byte[] echo(final byte[] string) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.echo(string);
        }
    }


    public Long linsert(final byte[] key, final ListPosition where, final byte[] pivot,
                        final byte[] value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.linsert(key, where, pivot, value);
        }
    }


    public String debug(final DebugParams params) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.debug(params);
        }
    }


    public Client getClient() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.getClient();
        }
    }


    public byte[] brpoplpush(final byte[] source, final byte[] destination, final int timeout) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.brpoplpush(source, destination, timeout);
        }
    }


    public Boolean setbit(final byte[] key, final long offset, final boolean value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.setbit(key, offset, value);
        }
    }


    public Boolean setbit(final byte[] key, final long offset, final byte[] value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.setbit(key, offset, value);
        }
    }


    public Boolean getbit(final byte[] key, final long offset) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.getbit(key, offset);
        }
    }


    public Long bitpos(final byte[] key, final boolean value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.bitpos(key, value);
        }
    }


    public Long bitpos(final byte[] key, final boolean value, final BitPosParams params) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.bitpos(key, value, params);
        }
    }


    public Long setrange(final byte[] key, final long offset, final byte[] value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.setrange(key, offset, value);
        }
    }


    public byte[] getrange(final byte[] key, final long startOffset, final long endOffset) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.getrange(key, startOffset, endOffset);
        }
    }


    public Long publish(final byte[] channel, final byte[] message) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.publish(channel, message);
        }
    }


    public void subscribe(BinaryJedisPubSub jedisPubSub, final byte[]... channels) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.subscribe(jedisPubSub, channels);
        }
    }


    public void psubscribe(BinaryJedisPubSub jedisPubSub, final byte[]... patterns) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.psubscribe(jedisPubSub, patterns);
        }
    }


    public int getDB() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.getDB();
        }
    }


    public Object eval(final byte[] script, final List<byte[]> keys, final List<byte[]> args) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.eval(script, keys, args);
        }
    }


    public Object eval(final byte[] script, final byte[] keyCount, final byte[]... params) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.eval(script, keyCount, params);
        }
    }


    public Object eval(final byte[] script, final int keyCount, final byte[]... params) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.eval(script, keyCount, params);
        }
    }


    public Object eval(final byte[] script) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.eval(script);
        }
    }


    public Object evalsha(final byte[] sha1) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.evalsha(sha1);
        }
    }


    public Object evalsha(final byte[] sha1, final List<byte[]> keys, final List<byte[]> args) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.evalsha(sha1, keys, args);
        }
    }


    public Object evalsha(final byte[] sha1, final int keyCount, final byte[]... params) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.evalsha(sha1, keyCount, params);
        }
    }


    public String scriptFlush() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.scriptFlush();
        }
    }


    public Long scriptExists(final byte[] sha1) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.scriptExists(sha1);
        }
    }


    public List<Long> scriptExists(final byte[]... sha1) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.scriptExists(sha1);
        }
    }


    public byte[] scriptLoad(final byte[] script) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.scriptLoad(script);
        }
    }


    public String scriptKill() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.scriptKill();
        }
    }


    public String slowlogReset() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.slowlogReset();
        }
    }


    public Long slowlogLen() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.slowlogLen();
        }
    }


    public List<byte[]> slowlogGetBinary() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.slowlogGetBinary();
        }
    }


    public List<byte[]> slowlogGetBinary(final long entries) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.slowlogGetBinary(entries);
        }
    }


    public Long objectRefcount(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.objectRefcount(key);
        }
    }


    public byte[] objectEncoding(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.objectEncoding(key);
        }
    }


    public Long objectIdletime(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.objectIdletime(key);
        }
    }


    public Long bitcount(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.bitcount(key);
        }
    }


    public Long bitcount(final byte[] key, final long start, final long end) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.bitcount(key, start, end);
        }
    }


    public Long bitop(final BitOP op, final byte[] destKey, final byte[]... srcKeys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.bitop(op, destKey, srcKeys);
        }
    }


    public byte[] dump(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.dump(key);
        }
    }


    public String restore(final byte[] key, final int ttl, final byte[] serializedValue) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.restore(key, ttl, serializedValue);
        }
    }


    public String restoreReplace(final byte[] key, final int ttl, final byte[] serializedValue) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.restoreReplace(key, ttl, serializedValue);
        }
    }


    public Long pexpire(final byte[] key, final long milliseconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pexpire(key, milliseconds);
        }
    }


    public Long pexpireAt(final byte[] key, final long millisecondsTimestamp) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pexpireAt(key, millisecondsTimestamp);
        }
    }


    public Long pttl(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pttl(key);
        }
    }


    public String psetex(final byte[] key, final long milliseconds, final byte[] value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.psetex(key, milliseconds, value);
        }
    }


    public byte[] memoryDoctorBinary() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.memoryDoctorBinary();
        }
    }


    public String clientKill(final byte[] ipPort) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clientKill(ipPort);
        }
    }


    public String clientKill(final String ip, final int port) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clientKill(ip, port);
        }
    }


    public Long clientKill(ClientKillParams params) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clientKill(params);
        }
    }


    public byte[] clientGetnameBinary() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clientGetnameBinary();
        }
    }


    public byte[] clientListBinary() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clientListBinary();
        }
    }


    public String clientSetname(final byte[] name) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clientSetname(name);
        }
    }


    public String clientPause(final long timeout) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.clientPause(timeout);
        }
    }


    public List<String> time() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.time();
        }
    }


    public String migrate(final String host, final int port, final byte[] key,
                          final int destinationDb, final int timeout) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.migrate(host, port, key, destinationDb, timeout);
        }
    }


    public String migrate(final String host, final int port, final int destinationDB,
                          final int timeout, final MigrateParams params, final byte[]... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.migrate(host, port, destinationDB, timeout, params, keys);
        }
    }


    public Long waitReplicas(final int replicas, final long timeout) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.waitReplicas(replicas, timeout);
        }
    }


    public Long pfadd(final byte[] key, final byte[]... elements) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pfadd(key, elements);
        }
    }


    public long pfcount(final byte[] key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pfcount(key);
        }
    }


    public String pfmerge(final byte[] destkey, final byte[]... sourcekeys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pfmerge(destkey, sourcekeys);
        }
    }


    public Long pfcount(final byte[]... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pfcount(keys);
        }
    }


    public ScanResult<byte[]> scan(final byte[] cursor) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.scan(cursor);
        }
    }


    public ScanResult<byte[]> scan(final byte[] cursor, final ScanParams params) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.scan(cursor, params);
        }
    }


    public ScanResult<Map.Entry<byte[], byte[]>> hscan(final byte[] key, final byte[] cursor) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hscan(key, cursor);
        }
    }


    public ScanResult<Map.Entry<byte[], byte[]>> hscan(final byte[] key, final byte[] cursor,
                                                       final ScanParams params) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hscan(key, cursor, params);
        }
    }


    public ScanResult<byte[]> sscan(final byte[] key, final byte[] cursor) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sscan(key, cursor);
        }
    }


    public ScanResult<byte[]> sscan(final byte[] key, final byte[] cursor, final ScanParams params) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sscan(key, cursor, params);
        }
    }


    public ScanResult<Tuple> zscan(final byte[] key, final byte[] cursor) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zscan(key, cursor);
        }
    }


    public ScanResult<Tuple> zscan(final byte[] key, final byte[] cursor, final ScanParams params) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zscan(key, cursor, params);
        }
    }


    public Long geoadd(final byte[] key, final double longitude, final double latitude, final byte[] member) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.geoadd(key, longitude, latitude, member);
        }
    }


    public Long geoadd(final byte[] key, final Map<byte[], GeoCoordinate> memberCoordinateMap) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.geoadd(key, memberCoordinateMap);
        }
    }


    public Double geodist(final byte[] key, final byte[] member1, final byte[] member2) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.geodist(key, member1, member2);
        }
    }


    public Double geodist(final byte[] key, final byte[] member1, final byte[] member2, final GeoUnit unit) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.geodist(key, member1, member2, unit);
        }
    }


    public List<byte[]> geohash(final byte[] key, final byte[]... members) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.geohash(key, members);
        }
    }


    public List<GeoCoordinate> geopos(final byte[] key, final byte[]... members) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.geopos(key, members);
        }
    }


    public List<GeoRadiusResponse> georadius(final byte[] key, final double longitude, final double latitude,
                                             final double radius, final GeoUnit unit) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.georadius(key, longitude, latitude, radius, unit);
        }
    }


    public List<GeoRadiusResponse> georadiusReadonly(final byte[] key, final double longitude, final double latitude,
                                                     final double radius, final GeoUnit unit) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.georadiusReadonly(key, longitude, latitude, radius, unit);
        }
    }


    public List<GeoRadiusResponse> georadius(final byte[] key, final double longitude, final double latitude,
                                             final double radius, final GeoUnit unit, final GeoRadiusParam param) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.georadius(key, longitude, latitude, radius, unit, param);
        }
    }


    public List<GeoRadiusResponse> georadiusReadonly(final byte[] key, final double longitude, final double latitude,
                                                     final double radius, final GeoUnit unit, final GeoRadiusParam param) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.georadiusReadonly(key, longitude, latitude, radius, unit, param);
        }
    }


    public List<GeoRadiusResponse> georadiusByMember(final byte[] key, final byte[] member, final double radius,
                                                     final GeoUnit unit) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.georadiusByMember(key, member, radius, unit);
        }
    }


    public List<GeoRadiusResponse> georadiusByMemberReadonly(final byte[] key, final byte[] member, final double radius,
                                                             final GeoUnit unit) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.georadiusByMemberReadonly(key, member, radius, unit);
        }
    }


    public List<GeoRadiusResponse> georadiusByMember(final byte[] key, final byte[] member, final double radius,
                                                     final GeoUnit unit, final GeoRadiusParam param) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.georadiusByMember(key, member, radius, unit, param);
        }
    }


    public List<GeoRadiusResponse> georadiusByMemberReadonly(final byte[] key, final byte[] member, final double radius,
                                                             final GeoUnit unit, final GeoRadiusParam param) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.georadiusByMemberReadonly(key, member, radius, unit, param);
        }
    }


}
