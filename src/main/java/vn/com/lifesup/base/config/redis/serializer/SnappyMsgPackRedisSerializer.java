package vn.com.lifesup.base.config.redis.serializer;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.xerial.snappy.Snappy;
import vn.com.lifesup.base.config.SpringApplicationContext;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class SnappyMsgPackRedisSerializer<T> implements RedisSerializer<T> {

    public static final Charset DEFAULT_CHARSET;

    static {
        DEFAULT_CHARSET = StandardCharsets.UTF_8;
    }

    private final JavaType javaType;
    private final ObjectMapper objectMapper;

    public SnappyMsgPackRedisSerializer(Class<T> type) {
        this.javaType = JavaTypeHandler.getJavaType(type);
        this.objectMapper = SpringApplicationContext.bean(ObjectMapper.class);
    }

    @Override
    public T deserialize(@Nullable byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        } else {
            try {
                final byte[] uncompressBytes = Snappy.uncompress(bytes);
                return this.objectMapper.readValue(uncompressBytes, 0, uncompressBytes.length, this.javaType);
            } catch (Exception ex) {
                throw new SerializationException("Could not read MsgPack JSON: " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public byte[] serialize(@Nullable Object value) throws SerializationException {
        if (value == null) {
            return new byte[0];
        } else {
            try {
                final byte[] bytes = this.objectMapper.writeValueAsBytes(value);
                return Snappy.compress(bytes);
            } catch (Exception ex) {
                throw new SerializationException("Could not write MsgPack JSON: " + ex.getMessage(), ex);
            }
        }
    }
}
