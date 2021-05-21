package com.mih.webauthn;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({WebAuthnConfig.class})
@EnableConfigurationProperties(AppProperties.class)
@Configuration
public @interface EnableWebAuthn {
    boolean debug() default false;
}
