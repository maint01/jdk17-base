package vn.com.lifesup.base.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringApplicationContext implements ApplicationContextAware {

  private static final String ERR_MSG = "Spring utility class not initialized";

  private static ApplicationContext context = null;

  @Override
  public synchronized void setApplicationContext(@NotNull ApplicationContext applicationContext)
      throws BeansException {
    context = applicationContext;
  }

  public static <T> T bean(Class<T> clazz) {
    if (context == null) {
      throw new IllegalStateException(ERR_MSG);
    }
    return context.getBean(clazz);
  }
}
