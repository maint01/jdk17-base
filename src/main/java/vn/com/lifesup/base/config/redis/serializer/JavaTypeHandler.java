package vn.com.lifesup.base.config.redis.serializer;


import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class JavaTypeHandler {
    private JavaTypeHandler() {}
    static <T> JavaType getJavaType(Class<T> clazz) {
        return TypeFactory.defaultInstance().constructType(clazz);
    }
}
