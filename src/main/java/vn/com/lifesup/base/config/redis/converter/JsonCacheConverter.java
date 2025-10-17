package vn.com.lifesup.base.config.redis.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonCacheConverter<T> implements CacheConverter<T> {

    private final ObjectMapper objectMapper;

    public JsonCacheConverter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public JsonCacheConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Object serialize(T value) throws Exception {
        if (value == null) {
            return null;
        }
        return objectMapper.writeValueAsString(value);
    }

    @Override
    public T deserialize(Object cachedValue, Class<T> targetType) throws Exception {
        if (cachedValue == null) {
            return null;
        }

        if (cachedValue instanceof String) {
            return objectMapper.readValue((String) cachedValue, targetType);
        }

        // If already deserialized object
        if (targetType.isAssignableFrom(cachedValue.getClass())) {
            return targetType.cast(cachedValue);
        }

        // Convert using ObjectMapper
        return objectMapper.convertValue(cachedValue, targetType);
    }
}
