package ink.andromeda.dataflow.cache;

import java.util.List;

public interface CacheService<K, V, C extends CacheContainer<K, V>> {

    /**
     * 获取缓存
     * @param k 缓存的key
     * @return 缓存内容
     */
    V get(K k);

    /**
     * 添加缓存
     * @param k 缓存的key
     * @param v 缓存的value
     */
    default void put(K k, V v){
        put(k, v, -1);
    }

    /**
     * 添加缓存以及过期时间
     * @param k 缓存的key
     * @param v 缓存的value
     * @param seconds 缓存时间, -1代表永不过期
     */
    void put(K k, V v, int seconds);

    /**
     * 移除缓存
     * @param k 要移除的缓存的key
     * @return 被移除的缓存内容
     */
    V remove(K k);

    /**
     * 批量移除
     * @param keys 要删除的key
     * @return 删除数量
     */
    int remove(List<K> keys);

    /**
     * 清除所有缓存
     * @return 被清除的数量
     */
    int clear();

    /**
     * 获取缓存信息
     * @param k 缓存的key
     * @return {@link CacheInfo}
     */
    CacheInfo<K> getCacheInfo(K k);

    /**
     * @return 所有的缓存信息
     */
    List<CacheInfo<K>> getAllCacheInfo();

    /**
     * 设置缓存删除策略
     * @param expiredPolicy 删除策略
     */
    void setCacheExpiredPolicy(CacheExpiredPolicy<K> expiredPolicy);

    CacheExpiredPolicy<K> getCacheExpiredPolicy();

}
