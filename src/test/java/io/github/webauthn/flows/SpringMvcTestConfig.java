package io.github.webauthn.flows;

import io.github.webauthn.EnableWebAuthn;
import io.github.webauthn.WebAuthnProperties;
import io.github.webauthn.config.WebAuthnConfigurer;
import io.github.webauthn.domain.WebAuthnUser;
import io.github.webauthn.domain.WebAuthnUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

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
                        .userSupplier(() -> {
                            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
                            if (token == null)
                                return null;
                            return userRepository.findByUsername(token.getName())
                                    .orElseThrow();
                        })
                        .defaultLoginSuccessHandler((user, credentials) -> log.info("login - user: {} with credentials: {}", user, credentials))
                        .registerSuccessHandler(user -> log.info("registerSuccessHandler - user: {}", user))
                );
    }
}
