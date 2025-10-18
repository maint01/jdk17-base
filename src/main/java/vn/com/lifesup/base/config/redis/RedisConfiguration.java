package vn.com.lifesup.base.config.redis;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import vn.com.lifesup.base.config.properties.RedisProperties;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RedisConfiguration {

    private final RedisProperties redisProperties;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        if ("sentinel".equalsIgnoreCase(redisProperties.getMode())) {
            return sentinelConfig();
        }
        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration();
        standaloneConfig.setHostName(redisProperties.getStandAloneHost());
        standaloneConfig.setPort(redisProperties.getStandAlonePort());
        return new LettuceConnectionFactory(standaloneConfig);
    }

    private LettuceConnectionFactory sentinelConfig() {
        final SocketOptions socketOptions =
                SocketOptions.builder().connectTimeout(Duration.ofSeconds(30)).build();

        ClientOptions clientOptions =
                ClientOptions.builder()
                        .socketOptions(socketOptions)
                        .autoReconnect(true)
                        .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                        .build();

        LettuceClientConfiguration clientConfig =
                LettuceClientConfiguration.builder()
                        .commandTimeout(Duration.ofSeconds(30))
                        .clientOptions(clientOptions)
                        .build();

        RedisSentinelConfiguration sentinelConfig =
                new RedisSentinelConfiguration(
                        redisProperties.getSentinel().getNameNode(),
                        redisProperties.getSentinel().getSentinelNodes());
        sentinelConfig.setSentinelPassword(redisProperties.getSentinel().getSentinelPassword());
        sentinelConfig.setPassword(redisProperties.getSentinel().getRedisPassword());

        LettuceConnectionFactory lettuceConnectionFactory =
                new LettuceConnectionFactory(sentinelConfig, clientConfig);

        lettuceConnectionFactory.setValidateConnection(true);

        // Add this line to prevent sharing native connections
        lettuceConnectionFactory.setShareNativeConnection(true);

        return lettuceConnectionFactory;
    }


    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Configure serializers
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    @Bean(name = "redisMessage")
    public RedisTemplate<String, Object> redisMessage(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}
