package vn.com.lifesup.base.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "application.cors", ignoreUnknownFields = false)
public class CorsProperties extends CorsConfiguration {
}
