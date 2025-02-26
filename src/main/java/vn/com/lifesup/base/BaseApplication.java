package vn.com.lifesup.base;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import vn.com.lifesup.base.config.SecurityProperties;

@EnableJpaRepositories(basePackages = "vn.com.lifesup.base.repository")
@EnableConfigurationProperties({LiquibaseProperties.class, SecurityProperties.class})
@SpringBootApplication
public class BaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaseApplication.class, args);
    }

}
