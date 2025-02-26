package vn.com.lifesup.base.config;

import lombok.Setter;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;

public class HeaderLocaleResolver implements LocaleResolver {

    @Setter
    private Locale defaultLocale;

    private String headerName;

    public static final String LOCALE_REQUEST_ATTRIBUTE_NAME = "LANG_KEY.LOCALE";

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        parseAngularHeaderIfNecessary(request);
        return (Locale) request.getAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME);
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        request.setAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME, (locale != null ? locale : defaultLocale));
    }

    public void setHeaderLangName(String headerName) {
        this.headerName = headerName;
    }

    private void parseAngularHeaderIfNecessary(HttpServletRequest request) {
        if (request.getAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME) == null) {
            Locale locale;
            String localeStr = request.getHeader(headerName);
            if (localeStr == null) locale = defaultLocale;
            else {
                locale = Locale.forLanguageTag(localeStr);
            }

            request.setAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME, locale);
        }
    }

}
