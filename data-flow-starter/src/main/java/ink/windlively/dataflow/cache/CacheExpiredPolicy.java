package ink.windlively.dataflow.cache;

import java.util.List;

/**
 * 缓存删除策略
 * @param <K>
 */
@FunctionalInterface
public interface CacheExpiredPolicy<K> {

    List<K> getCanRemovedCache(List<CacheInfo<K>> cacheInfo);

}
