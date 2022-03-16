package io.github.webauthn;


import io.github.webauthn.domain.WebAuthnCredentialsInMemoryRepository;
import io.github.webauthn.domain.WebAuthnCredentialsRepository;
import io.github.webauthn.domain.WebAuthnUserInMemoryRepository;
import io.github.webauthn.domain.WebAuthnUserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebAuthnInMemoryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(WebAuthnUserRepository.class)
    public WebAuthnUserRepository webAuthnUserRepository() {
        return new WebAuthnUserInMemoryRepository();
    }

    @Bean
    @ConditionalOnMissingBean(WebAuthnCredentialsRepository.class)
    public WebAuthnCredentialsRepository webAuthnCredentialsRepository() {
        return new WebAuthnCredentialsInMemoryRepository();
    }
}
