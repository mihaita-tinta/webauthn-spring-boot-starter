package io.github.webauthn.webflux;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.webauthn.RelyingParty;
import io.github.webauthn.WebAuthnProperties;
import io.github.webauthn.config.WebAuthnOperation;
import io.github.webauthn.domain.WebAuthnCredentials;
import io.github.webauthn.domain.WebAuthnCredentialsRepository;
import io.github.webauthn.domain.WebAuthnUser;
import io.github.webauthn.domain.WebAuthnUserRepository;
import io.github.webauthn.events.WebAuthnEventPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.server.WebHandler;

import java.util.function.Supplier;

@Configuration
@ConditionalOnClass(WebHandler.class)
@ConditionalOnProperty(value = "spring.main.web-application-type", havingValue = "reactive")
public class WebAuthnWebFluxConfig {

    @Bean
    @ConditionalOnMissingBean
    public WebSessionServerSecurityContextRepository serverSecurityContextRepository() {
        return new WebSessionServerSecurityContextRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    public WebAuthnErrorWebExceptionHandler webAuthnErrorWebExceptionHandler() {
        return new WebAuthnErrorWebExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public Supplier<WebAuthnWebFilter> webAuthnWebFilterSupplier(WebAuthnProperties properties,
                                                                 WebAuthnUserRepository<WebAuthnUser> webAuthnUserRepository,
                                                                 WebAuthnCredentialsRepository<WebAuthnCredentials> credentialsRepository,
                                                                 RelyingParty rp,
                                                                 ObjectMapper mapper,
                                                                 WebAuthnOperation registration,
                                                                 WebAuthnOperation assertion,
                                                                 ServerSecurityContextRepository serverSecurityContextRepository,
                                                                 WebAuthnEventPublisher publisher) {
        return () -> new WebAuthnWebFilter(properties,
                webAuthnUserRepository,
                credentialsRepository,
                rp,
                mapper, registration, assertion, serverSecurityContextRepository,
                publisher);
    }

}
