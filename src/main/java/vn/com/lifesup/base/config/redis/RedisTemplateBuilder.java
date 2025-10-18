package vn.com.lifesup.base.config.redis;

import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import vn.com.lifesup.base.config.redis.serializer.SnappyMsgPackRedisSerializer;

public class RedisTemplateBuilder {
    public static <T> RedisTemplate<String, T> getSnappyRedisTemplate(
            final LettuceConnectionFactory factory,
            final Class<T> clazz) {

        SnappyMsgPackRedisSerializer<T> snappyMsgPackSerializer = new SnappyMsgPackRedisSerializer<>(clazz);
        RedisTemplate<String, T> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setDefaultSerializer(new JdkSerializationRedisSerializer()); // Loi Parse BigDecimal do SnappyHash
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(snappyMsgPackSerializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        redisTemplate.setHashValueSerializer(new JdkSerializationRedisSerializer()); // Loi Parse BigDecimal do SnappyHash

        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    public static <T> RedisTemplate<String, T> getDefaultRedisTemplate(final LettuceConnectionFactory factory) {
        RedisTemplate<String, T> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }
}
