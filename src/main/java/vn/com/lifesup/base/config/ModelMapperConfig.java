package vn.com.lifesup.base.config;


import org.apache.commons.lang3.StringUtils;
import org.modelmapper.*;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.com.lifesup.base.util.DateUtil;

import java.time.Instant;

@Configuration
public class ModelMapperConfig {

    private final Provider<Instant> instantProvider = new AbstractProvider<Instant>() {
        @Override
        protected Instant get() {
            return Instant.now();
        }
    };

    private final Converter<String, Instant> convertStringToInstant = new AbstractConverter<String, Instant>() {
        @Override
        protected Instant convert(String s) {
            if (StringUtils.isEmpty(s)) {
                return null;
            }
            boolean isFullDate = ConfigConstants.DATETIME_FORMAT.length() == s.length();
            return DateUtil.convertToInstant(s, isFullDate);
        }
    };

    private final Converter<Instant, String> convertInstantToString = new AbstractConverter<Instant, String>() {
        @Override
        protected String convert(Instant date) {
            return DateUtil.convertToString(date, true);
        }
    };

    private final Provider<String> stringProvider = new AbstractProvider<String>() {
        @Override
        protected String get() {
            return Instant.now().toString();
        }
    };

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true);
        mapper.createTypeMap(String.class, Instant.class);
        mapper.addConverter(convertStringToInstant);
        mapper.getTypeMap(String.class, Instant.class).setProvider(instantProvider);
        mapper.createTypeMap(Instant.class, String.class);
        mapper.addConverter(convertInstantToString);
        mapper.getTypeMap(Instant.class, String.class).setProvider(stringProvider);
        return mapper;
    }

}