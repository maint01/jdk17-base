package vn.com.lifesup.base.util;

import jakarta.annotation.Nullable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import vn.com.lifesup.base.config.SpringApplicationContext;

import java.util.Arrays;
import java.util.Locale;

public class Translator {
    private Translator() {}

    public static String getMessage(String code, @Nullable Object[] args) {
        ResourceBundleMessageSource messageSource = SpringApplicationContext.bean(ResourceBundleMessageSource.class);
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, args, locale);
    }

    public static String getMessage(String code, String... args) {
        ResourceBundleMessageSource messageSource = SpringApplicationContext.bean(ResourceBundleMessageSource.class);
        Locale locale = LocaleContextHolder.getLocale();
        if (args != null && args.length > 0) {
            args = Arrays.stream(args).map(Translator::getMessage).toArray(String[]::new);
        }
        return messageSource.getMessage(code, args, locale);
    }

    public static String getMessage(String code) {
        ResourceBundleMessageSource messageSource = SpringApplicationContext.bean(ResourceBundleMessageSource.class);
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, null, locale);
    }

    public static String getMessage(ErrorCode errorCode, @Nullable Object[] args) {
        ResourceBundleMessageSource messageSource = SpringApplicationContext.bean(ResourceBundleMessageSource.class);
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(errorCode.getMessage(), args, locale);
    }

    public static String getMessage(ErrorCode errorCode, String... args) {
        ResourceBundleMessageSource messageSource = SpringApplicationContext.bean(ResourceBundleMessageSource.class);
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(errorCode.getMessage(), args, locale);
    }

    public static String getMessage(ErrorCode errorCode) {
        ResourceBundleMessageSource messageSource = SpringApplicationContext.bean(ResourceBundleMessageSource.class);
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(errorCode.getMessage(), null, locale);
    }
}
