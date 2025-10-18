package vn.com.lifesup.base.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "application.redis")
public class RedisProperties {
    private String mode;
    private String standAloneHost;
    private int standAlonePort;
    private Sentinel sentinel;
    private Topic topic;

    @Getter
    @Setter
    @Configuration
    @ConfigurationProperties(prefix = "application.redis.sentinel")
    public static class Sentinel {
        private String nameNode;
        private HashSet<String> sentinelNodes;
        private String sentinelPassword;
        private String redisPassword;
    }

    @Getter
    @Setter
    @Configuration
    @ConfigurationProperties(prefix = "application.redis.topic")
    public static class Topic {
        private String qrLogin;
        private String notification;
        private String layer2cache;
        private String feedback;
    }
}
