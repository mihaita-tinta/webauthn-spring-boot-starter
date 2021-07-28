package com.mih.webauthn;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mih.webauthn.config.WebauthnConfigurer;
import com.mih.webauthn.domain.WebAuthnDefaultUser;
import com.mih.webauthn.domain.WebAuthnUser;
import com.mih.webauthn.domain.WebAuthnUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

@SpringBootApplication
@EnableConfigurationProperties(WebAuthnProperties.class)
public class TestConfig extends WebSecurityConfigurerAdapter {
    private static final Logger log = LoggerFactory.getLogger(TestConfig.class);
    @Autowired
    WebAuthnUserRepository userRepository;

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
                .apply(new WebauthnConfigurer()
                        .userSupplier(() -> {
                            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
                            return userRepository.findByUsername(token.getName())
                                    .orElseThrow();
                        })
                        .defaultLoginSuccessHandler(user -> log.info("login - user: {}", user))
                        .registerSuccessHandler(user -> log.info("registerSuccessHandler - user: {}", user))
                );
    }
}
