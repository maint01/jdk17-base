package vn.com.lifesup.base.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Properties specific to Admin Api.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 */
@Getter
@ConfigurationProperties(prefix = "application.security", ignoreUnknownFields = false)
public class SecurityProperties {
    private SecurityProperties() {}
    @Setter
    private String contentSecurityPolicy = "default-src 'self'; frame-src 'self' data:; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:";

    private final ClientAuthorization clientAuthorization = new ClientAuthorization();

    private final Authentication authentication = new Authentication();

    private final RememberMe rememberMe = new RememberMe();

    private final OAuth2 oauth2 = new OAuth2();

    @Setter
    @Getter
    public static class ClientAuthorization {

        private String accessTokenUri = null;

        private String tokenServiceId = null;

        private String clientId = null;

        private String clientSecret = null;

    }

    @Getter
    public static class Authentication {

        private final Jwt jwt = new Jwt();

        @Setter
        @Getter
        public static class Jwt {

            private String secret = "default-src 'self'; frame-src 'self' data:; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:";

            private String base64Secret = null;

            private long tokenValidityInSeconds = 1800;// 30 minutes

            private long tokenValidityInSecondsForRememberMe = 2592000; // 30 days

        }
    }

    @Setter
    @Getter
    public static class RememberMe {

        @NotNull
        private String key = null;

    }

    public static class OAuth2 {
        private final List<String> audience = new ArrayList<>();

        public List<String> getAudience() {
            return Collections.unmodifiableList(audience);
        }

        public void setAudience(@NotNull List<String> audience) {
            this.audience.addAll(audience);
        }
    }
}
