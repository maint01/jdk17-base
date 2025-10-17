package vn.com.lifesup.base.config.redis.converter;

public interface CacheConverter<T> {

    /**
     * Serialize object to store in cache
     */
    Object serialize(T value) throws Exception;

    /**
     * Deserialize object from cache
     */
    T deserialize(Object cachedValue, Class<T> targetType) throws Exception;
}
