package vn.com.lifesup.base.config.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import vn.com.lifesup.base.config.redis.converter.CacheConverter;

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
            throw new IllegalArgumentException("Cache name cannot be null or empty");
        }
        if (valueType == null) {
            throw new IllegalArgumentException("Value type cannot be null");
        }
        if (redisTemplate == null) {
            throw new IllegalArgumentException("RedisTemplate cannot be null");
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


}
