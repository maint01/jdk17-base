package vn.com.lifesup.base.security;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.com.lifesup.base.model.Authority;
import vn.com.lifesup.base.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Authenticate a user from the database.
 */
@RequiredArgsConstructor
@Component("userDetailsService")
public class DomainUserDetailsService implements UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(DomainUserDetailsService.class);

    private final UserRepository userRepository;


    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String login) {
        log.debug("Authenticating {}", login);

        String lowercaseLogin = login.toLowerCase(Locale.ENGLISH);
        return userRepository
            .findOneByUsername(lowercaseLogin)
            .map(this::createSpringSecurityUser)
            .orElseThrow(() -> new UsernameNotFoundException("User " + login + " not found in database"));
    }

    private UserJwt createSpringSecurityUser(vn.com.lifesup.base.model.User user) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        Set<Authority> authorities = user.getAuthorities();
        authorities.forEach(authority -> grantedAuthorities.add(new SimpleGrantedAuthority(authority.getName())));
        UserJwt userJwt = new UserJwt();
        userJwt.setUsername(user.getUsername());
        userJwt.setPassword(user.getPassword());
        userJwt.setGrantedAuthorities(grantedAuthorities);
        userJwt.setStatus(user.isActivated() ? 1 : 0);
        return userJwt;
    }
}
