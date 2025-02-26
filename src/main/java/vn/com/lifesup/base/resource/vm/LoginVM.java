package vn.com.lifesup.base.resource.vm;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * View Model object for storing a user's credentials.
 */
@Getter
@Setter
public class LoginVM {

    @NotNull(message = "ERROR_1013")
    private String username;

    @NotNull(message = "ERROR_1012")
    private String password;

    private Boolean rememberMe;
}
