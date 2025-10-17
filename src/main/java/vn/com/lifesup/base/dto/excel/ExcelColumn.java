package vn.com.lifesup.base.dto.excel;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcelColumn {
    int index();
    boolean notNull() default false;
    int minLength() default -1;
    int maxLength() default -1;
    String fieldName() default StringUtils.EMPTY;
    String pattern() default StringUtils.EMPTY;
}
