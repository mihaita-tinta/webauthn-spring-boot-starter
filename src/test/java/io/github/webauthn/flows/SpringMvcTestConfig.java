package io.github.webauthn.flows;

import io.github.webauthn.EnableWebAuthn;
import io.github.webauthn.config.WebAuthnConfigurer;
import io.github.webauthn.domain.DefaultWebAuthnUser;
import io.github.webauthn.domain.WebAuthnUser;
import io.github.webauthn.domain.WebAuthnUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import java.util.Map;
import java.util.Optional;

@SpringBootApplication
@EnableWebAuthn
@EnableWebSecurity
public class SpringMvcTestConfig {
    private static final Logger log = LoggerFactory.getLogger(SpringMvcTestConfig.class);
    @Autowired
    WebAuthnUserRepository<DefaultWebAuthnUser> userRepository;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
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

        return http.build();
    }
}
