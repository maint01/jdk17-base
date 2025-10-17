package vn.com.lifesup.base.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import vn.com.lifesup.base.constant.ConfigConstants;

import java.util.Locale;

@Configuration
public class LocaleConfiguration implements WebMvcConfigurer {

    @Bean
    public LocaleResolver localeResolver() {
        HeaderLocaleResolver headerLocaleResolver = new HeaderLocaleResolver();
        headerLocaleResolver.setHeaderLangName(ConfigConstants.LANG_KEY);
        headerLocaleResolver.setDefaultLocale(Locale.forLanguageTag(ConfigConstants.DEFAULT_LANGUAGE));
        return headerLocaleResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("language");
        registry.addInterceptor(localeChangeInterceptor);
    }
    
    
}
