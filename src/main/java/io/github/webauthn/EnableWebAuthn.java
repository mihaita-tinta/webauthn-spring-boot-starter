package io.github.webauthn;

import io.github.webauthn.webflux.WebAuthnWebFluxConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @see io.github.webauthn.config.WebAuthnConfigurer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({WebAuthnConfig.class, WebAuthnWebFluxConfig.class})
@Configuration
public @interface EnableWebAuthn {
    boolean debug() default false;
}
