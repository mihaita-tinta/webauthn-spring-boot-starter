package com.mih.webauthn;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mih.webauthn.config.WebauthnConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

@SpringBootApplication
@EnableConfigurationProperties(WebAuthnProperties.class)
public class TestConfig extends WebSecurityConfigurerAdapter {
    private static final Logger log = LoggerFactory.getLogger(TestConfig.class);

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

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
                        .defaultLoginSuccessHandler(user -> log.info("login - user: {}", user))
                        .registerSuccessHandler(user -> log.info("registerSuccessHandler - user: {}", user))
                );
    }
}
