package io.github.webauthn.flows;

import io.github.webauthn.EnableWebAuthn;
import io.github.webauthn.config.WebAuthnConfigurer;
import io.github.webauthn.domain.WebAuthnUser;
import io.github.webauthn.domain.WebAuthnUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import java.util.Map;
import java.util.Optional;

@SpringBootApplication
@EnableWebAuthn
public class SpringMvcTestConfig extends WebSecurityConfigurerAdapter {
    private static final Logger log = LoggerFactory.getLogger(SpringMvcTestConfig.class);
    @Autowired
    WebAuthnUserRepository<WebAuthnUser> userRepository;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf(customizer -> customizer.disable())
                .logout(customizer -> {
                    customizer.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
                    customizer.deleteCookies("JSESSIONID");
                })
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
                .apply(new WebAuthnConfigurer()
                        .userSupplier(() ->
                                Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                                        .map(authn -> userRepository.findByUsername(authn.getName())
                                                .orElseThrow() //here you can migrate users in the webauthn user repository
                                        )
                                        .orElse(null) // registering a new user account for unauthenticated requests

                        )
                        .authenticationSuccessHandler((finish) -> Map.of("username", finish.getUser().getUsername()))
                        .defaultLoginSuccessHandler((user, credentials) -> log.info("login - user: {} with credentials: {}", user, credentials))
                        .registerSuccessHandler(user -> log.info("registerSuccessHandler - user: {}", user))
                );
    }
}
