package vn.com.lifesup.base.config.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import vn.com.lifesup.base.config.redis.converter.CacheConverter;
import vn.com.lifesup.base.dto.redis.CacheStats;
import vn.com.lifesup.base.exception.UncheckBusinessException;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
@Setter
@Log4j2
public class RedisCache<K, V> {

    private final String cacheName;
    private final Class<V> valueType;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final long defaultExpiration; // seconds
    private final CacheConverter<V> converter;

    /**
     * Constructor with default expiration (1 hour)
     */
    public RedisCache(String cacheName, Class<V> valueType, RedisTemplate<String, Object> redisTemplate) {
        this(cacheName, valueType, redisTemplate, 3600L, null); // 1 hour default
    }

    /**
     * Constructor with custom expiration
     */
    public RedisCache(String cacheName, Class<V> valueType, RedisTemplate<String, Object> redisTemplate, long defaultExpiration) {
        this(cacheName, valueType, redisTemplate, defaultExpiration, null);
    }

    /**
     * Constructor with custom converter
     */
    public RedisCache(String cacheName, Class<V> valueType, RedisTemplate<String, Object> redisTemplate, CacheConverter<V> converter) {
        this(cacheName, valueType, redisTemplate, 3600L, converter);
    }

    /**
     * Constructor with custom expiration and converter
     */
    public RedisCache(String cacheName, Class<V> valueType, RedisTemplate<String, Object> redisTemplate,
                      long defaultExpiration, CacheConverter<V> converter) {
        if (StringUtils.isBlank(cacheName)) {
            throw new UncheckBusinessException("Cache name cannot be null or empty");
        }
        if (valueType == null) {
            throw new UncheckBusinessException("Value type cannot be null");
        }
        if (redisTemplate == null) {
            throw new UncheckBusinessException("RedisTemplate cannot be null");
        }

        this.cacheName = cacheName;
        this.valueType = valueType;
        this.redisTemplate = redisTemplate;
        this.defaultExpiration = defaultExpiration;
        this.converter = converter;
        this.objectMapper = new ObjectMapper();

        // Configure Redis serializers
        this.redisTemplate.setKeySerializer(new StringRedisSerializer());
        this.redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        this.redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        this.redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
    }

    /**
     * Get value by key
     */
    public V get(K key) {
        try {
            String cacheKey = createCacheKey(key);
            Object value = redisTemplate.opsForHash().get(cacheName, cacheKey);
            return deserializeValue(value);
        } catch (Exception e) {
            log.error("Error getting value from cache: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get value by key with default fallback
     */
    public V get(K key, V defaultValue) {
        V value = get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Put value with default expiration
     */
    public V put(K key, V value) { return put(key, value, defaultExpiration); }

    /**
     * Put value with custom expiration
     */
    public V put(K key, V value, long expiration) {
        try {
            String cacheKey = createCacheKey(key);
            Object serializedValue = serializeValue(value);
            redisTemplate.opsForHash().put(cacheName, cacheKey, serializedValue);

            // Set expiration for the entire hash if it's a new key
            if (expiration > 0) {
                redisTemplate.expire(cacheName, expiration, TimeUnit.SECONDS);
            }

            log.debug("Cached value for key: {} in cache: {}", key, cacheName);
            return value;
        } catch (Exception e) {
            log.error("Error putting value to cache: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Put value only if key doesn't exist
     */
    public boolean putIfAbsent(K key, V value) { return putIfAbsent(key, value, defaultExpiration); }

    /**
     * Put value only if key doesn't exist with custom expiration
     */
    public boolean putIfAbsent(K key, V value, long expiration) {
        try {
            String cacheKey = createCacheKey(key);
            Object serializedValue = serializeValue(value);
            Boolean absent = redisTemplate.opsForHash().putIfAbsent(cacheName, cacheKey, serializedValue);

            if (Boolean.TRUE.equals(absent) && expiration > 0) {
                redisTemplate.expire(cacheName, expiration, TimeUnit.SECONDS);
            }

            return Boolean.TRUE.equals(absent);
        } catch (Exception e) {
            log.error("Error putting value if absent to cache: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Remove value by key
     */
    public V remove(K key) {
        try {
            String cacheKey = createCacheKey(key);
            V oldValue = get(key);
            redisTemplate.opsForHash().delete(cacheName, cacheKey);
            log.debug("Removed key: {} from cache: {}", key, cacheName);
            return oldValue;
        } catch (Exception e) {
            log.error("Error removing value from cache: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Remove multiple keys
     */
    public void removeAll(Collection<K> keys) {
        try {
            if (keys != null && !keys.isEmpty()) {
                String[] cacheKeys = keys.stream()
                        .map(this::createCacheKey)
                        .toArray(String[]::new);
                redisTemplate.opsForHash().delete(cacheName, (Object[]) cacheKeys);
                log.debug("Removed {} keys from cache: {}", keys.size(), cacheName);
            }
        } catch (Exception e) {
            log.error("Error removing multiple values from cache: {}", e.getMessage(), e);
        }
    }

    /**
     * Check if key exists
     */
    public boolean containsKey(K key) {
        try {
            String cacheKey = createCacheKey(key);
            return redisTemplate.opsForHash().hasKey(cacheName, cacheKey);
        } catch (Exception e) {
            log.error("Error checking key existence in cache: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Clear all entries in the cache
     */
    public void clear() {
        try {
            redisTemplate.delete(cacheName);
            log.debug("Cleared cache: {}", cacheName);
        } catch (Exception e) {
            log.error("Error clearing cache: {}", e.getMessage(), e);
        }
    }

    /**
     * Get cache size
     */
    public int size() {
        try {
            return redisTemplate.opsForHash().size(cacheName).intValue();
        } catch (Exception e) {
            log.error("Error getting cache size: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Check if cache is empty
     */
    public boolean isEmpty() { return size() == 0; }

    /**
     * Get all Keys
     * /unchecked
     */
    public Set<K> keys() {
        try {
            Set<Object> hashKeys = redisTemplate.opsForHash().keys(cacheName);
            return hashKeys.stream()
                    .map(key -> (K) parseKey(key.toString()))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Error getting keys from cache: {}", e.getMessage(), e);
            return Collections.emptySet();
        }
    }

    /**
     * Get all values
     */
    public Collection<V> values() {
        try {
            List<Object> hashValues = redisTemplate.opsForHash().values(cacheName);
            return hashValues.stream()
                    .map(this::deserializeValue)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception e) {
            log.error("Error getting values from cache: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Get all entries as Map
     */
    public Map<K, V> entries() {
        try {
            Map<Object, Object> hashEntries = redisTemplate.opsForHash().entries(cacheName);
            Map<K, V> result = new HashMap<>();

            for (Map.Entry<Object, Object> entry : hashEntries.entrySet()) {
                // Unchecked
                K key = (K) parseKey(entry.getKey().toString());
                V value = deserializeValue(entry.getValue());
                if (value != null) {
                    result.put(key, value);
                }
            }

            return result;
        } catch (Exception e) {
            log.error("Error getting entries from cache: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * Batch get multiple values
     */
    public Map<K, V> multiGet(Collection<K> keys) {
        try {
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyMap();
            }

            List<Object> cacheKeys = keys.stream()
                    .map(this::createCacheKey)
                    .collect(Collectors.toList());

            List<Object> values = redisTemplate.opsForHash().multiGet(cacheName, cacheKeys);
            Map<K, V> result = new HashMap<>();

            Iterator<K> keyIterator = keys.iterator();
            Iterator<Object> valueIterator = values.iterator();

            while (keyIterator.hasNext() && valueIterator.hasNext()) {
                K key = keyIterator.next();
                Object value = valueIterator.next();
                V deserializedValue = deserializeValue(value);
                if (deserializedValue != null) {
                    result.put(key, deserializedValue);
                }
            }

            return result;
        } catch (Exception e) {
            log.error("Error getting multiple values from cache: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * Batch put multiple values
     */
    public void multiPut(Map<K, V> entries) { multiPut(entries, defaultExpiration); }

    /**
     * Batch put multiple values with custom expiration
     */
    public void multiPut(Map<K, V> entries, long expiration) {
        try {
            if (entries == null || entries.isEmpty()) {
                return;
            }

            Map<String, Object> hashEntries = new HashMap<>();
            for (Map.Entry<K, V> entry : entries.entrySet()) {
                String cacheKey = createCacheKey(entry.getKey());
                Object serializedValue = serializeValue(entry.getValue());
                hashEntries.put(cacheKey, serializedValue);
            }

            redisTemplate.opsForHash().putAll(cacheName, hashEntries);

            if (expiration > 0) {
                redisTemplate.expire(cacheName, expiration, TimeUnit.SECONDS);
            }

            log.debug("Batch cached {} entries in cache: {}", entries.size(), cacheName);
        } catch (Exception e) {
            log.error("Error batch putting values to cache: {}", e.getMessage(), e);
        }
    }

    /**
     * Get cache statistics
     */
    public CacheStats getStats() {
        try {
            long size = redisTemplate.opsForHash().size(cacheName);
            Long ttl = redisTemplate.getExpire(cacheName, TimeUnit.SECONDS);
            return new CacheStats(cacheName, size, ttl != null ? ttl : -1);
        } catch (Exception e) {
            log.error("Error getting cache stats: {}", e.getMessage(), e);
            return new CacheStats(cacheName,  0, -1);
        }
    }

// Helper methods

    private String createCacheKey(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Cache key cannot be null");
        }
        return key.toString();
    }

    private Object parseKey(String keyString) {
        // Simple key parsing - can be enhanced based on key type
        return keyString;
    }

    // unchecked
    private V deserializeValue(Object value) {
        if (value == null) {
            return null;
        }

        try {
            // Use custom converter if provided
            if (converter != null) {
                return converter.deserialize(value, valueType);
            }

            if (valueType.isAssignableFrom(value.getClass())) {
                return (V) value;
            }

            // Handle string serialization
            if (value instanceof String) {
                String jsonValue = (String) value;
                if (valueType == String.class) {
                    return (V) jsonValue;
                }

                return objectMapper.readValue(jsonValue, valueType);
            }

            // Handle other types via Jackson
            return objectMapper.convertValue(value, valueType);
        } catch (Exception e) {
            log.error("Error deserializing value: {}", e.getMessage(), e);
            return null;
        }
    }

    private Object serializeValue(V value) {
        if (value == null) {
            return null;
        }

        try {
            // Use custom converter if provided
            if (converter != null) {
                return converter.serialize(value);
            }

            // Default serialization
            return value;
        } catch (Exception e) {
            log.error("Error serializing value: {}", e.getMessage(), e);
            return value;
        }
    }
}
