package vn.com.lifesup.base.config.properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.filter.CorsFilter;
import vn.com.lifesup.base.resource.error.AuthenticationEntryPointHandler;
import vn.com.lifesup.base.security.AuthoritiesConstants;
import vn.com.lifesup.base.security.jwt.JWTConfigurer;
import vn.com.lifesup.base.security.jwt.TokenProvider;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true, securedEnabled = true)
public class SecurityConfiguration {


    private final TokenProvider tokenProvider;
    
    private final UserDetailsService userDetailsService;

    private final CorsFilter corsFilter;
    private final AuthenticationEntryPointHandler authEntryPointHandler;

    public SecurityConfiguration(
        TokenProvider tokenProvider,
        CorsFilter corsFilter,
        UserDetailsService userDetailsService,
        AuthenticationEntryPointHandler authEntryPointHandler
    ) {
        this.tokenProvider = tokenProvider;
        this.corsFilter = corsFilter;
        this.authEntryPointHandler = authEntryPointHandler;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public WebSecurityCustomizer webSecurityCustomizer() {
//        return web -> web.ignoring()
//                .requestMatchers(HttpMethod.OPTIONS, "/**")
//                .requestMatchers("/swagger-ui/**")
//                .requestMatchers("/test/**");
//    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Vô hiệu hóa CSRF
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class) // Thêm bộ lọc CORS
                .exceptionHandling(auth -> auth
                        .authenticationEntryPoint(authEntryPointHandler)
                        .accessDeniedHandler(authEntryPointHandler))

                .headers(headers -> headers.contentSecurityPolicy(policyConfig ->
                        policyConfig.policyDirectives("default-src 'self'; frame-src 'self' data:; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com;style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:;"))
                .referrerPolicy(referrerPolicyConfig -> referrerPolicyConfig.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                .permissionsPolicyHeader(permissionsPolicyConfig -> permissionsPolicyConfig.policy(
                        "geolocation 'none'; midi 'none'; sync-xhr 'none'; microphone 'none'; camera 'none'; magnetometer 'none'; gyroscope 'none'; fullscreen 'self'; payment 'none'")))
                .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeHttpRequestsCustomizer ->
                        authorizeHttpRequestsCustomizer
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers("/swagger-ui/**").permitAll()
                            .requestMatchers("/api/authenticate").permitAll()
                            .requestMatchers("/api/register").permitAll()
                            .requestMatchers("/api/activate").permitAll()
                            .requestMatchers("/api/refresh-token").permitAll()
                            .requestMatchers("/api/account/reset-password/**").permitAll()
                            .requestMatchers("/api/account/**").authenticated()
                            .requestMatchers("/api/admin/**").hasAuthority(AuthoritiesConstants.ADMIN)
                            .requestMatchers("/api/**").authenticated()
                            .requestMatchers("/management/health").permitAll()
                            .requestMatchers("/management/health/**").permitAll()
                            .requestMatchers("/management/info").permitAll()
                            .requestMatchers("/management/prometheus").permitAll()
                            .requestMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN))
            .httpBasic(Customizer.withDefaults())
            .with(securityConfigurerAdapter(), Customizer.withDefaults());
        return http.build();
        // @formatter:on
    }

    private JWTConfigurer securityConfigurerAdapter() {
        return new JWTConfigurer(tokenProvider);
    }
    
    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    
}
