package vn.com.lifesup.base.config.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor
public class RedisCacheFactory<K, V> {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ConcurrentMap<String, RedisCache<?, ?>> cacheInstances = new ConcurrentHashMap<>();

    /**
     * Get or create a cache instance
     */
    @SuppressWarnings("unchecked")
    public <K, V> RedisCache<K, V> getCache(String cacheName, Class<V> valueType) {
        String cacheKey = cacheName + ":" + valueType.getName();
        return (RedisCache<K, V>) cacheInstances.computeIfAbsent(cacheKey,
                key -> new RedisCache<>(cacheName, valueType, redisTemplate));
    }

    /**
     * Get or create a cache instance with custom expiration
     */
    @SuppressWarnings("unchecked")
    public <K, V> RedisCache<K, V> getCache(String cacheName, Class<V> valueType, long expiration) {
        String cacheKey = cacheName + ":" + valueType.getName();
        return (RedisCache<K, V>) cacheInstances.computeIfAbsent(cacheKey,
                key -> new RedisCache<>(cacheName, valueType, redisTemplate, expiration));
    }

    /**
     * Remove cache instance
     */
    public void removeCache(String cacheName, Class<?> valueType) {
        String cacheKey = cacheName + ":" + valueType.getName();
        RedisCache<?, ?> cache = cacheInstances.remove(cacheKey);
        if (cache != null) {
            cache.clear();
        }
    }
}
