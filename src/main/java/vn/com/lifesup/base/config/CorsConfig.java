package vn.com.lifesup.base.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedMethods(Arrays.asList(HttpMethod.OPTIONS.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.GET.name(),
                HttpMethod.POST.name()));
        config.setMaxAge(31536000L);
        config.setAllowCredentials(false);
        config.setAllowedOrigins(Collections.singletonList("*"));
        config.setExposedHeaders(Arrays.asList("Content-Type", "Response-Content", "X-AUTH-TOKEN", "Authorization", "TZ-OFFSET"));
        config.setAllowedHeaders(Arrays.asList("Content-Type", "Cache-Control", "Origin", "Authorization", "TZ-OFFSET"));
        if (!CollectionUtils.isEmpty(config.getAllowedOrigins())) {
            source.registerCorsConfiguration("/api/**", config);
        }
        return new CorsFilter(source);
    }
}
