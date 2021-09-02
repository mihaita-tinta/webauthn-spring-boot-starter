package io.github.webauthn;


import io.github.webauthn.config.InMemoryOperation;
import io.github.webauthn.config.WebAuthnOperation;
import io.github.webauthn.domain.WebAuthnCredentialsInMemoryRepository;
import io.github.webauthn.domain.WebAuthnCredentialsRepository;
import io.github.webauthn.domain.WebAuthnUserInMemoryRepository;
import io.github.webauthn.domain.WebAuthnUserRepository;
import io.github.webauthn.dto.AssertionStartResponse;
import io.github.webauthn.dto.RegistrationStartResponse;
import io.github.webauthn.service.DefaultCredentialService;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
@EnableConfigurationProperties(WebAuthnProperties.class)
@AutoConfigureAfter(WebAuthnInMemoryAutoConfiguration.class)
public class WebAuthnConfig {

    @Bean
    @ConditionalOnMissingBean
    public WebAuthnOperation<RegistrationStartResponse, String> webAuthnRegistrationCache() {
        return new InMemoryOperation<>();
    }

    @Bean
    @ConditionalOnMissingBean
    public WebAuthnOperation<AssertionStartResponse, String> webAuthnAssertionCache() {
        return new InMemoryOperation();
    }

    @Bean
    public CredentialRepository credentialRepositoryService(WebAuthnCredentialsRepository credentialsRepository,
                                                            WebAuthnUserRepository userRepository) {
        return new DefaultCredentialService(credentialsRepository, userRepository);
    }

    @Bean
    @ConditionalOnMissingBean(RelyingParty.class)
    public RelyingParty relyingParty(CredentialRepository credentialRepository,
                                     WebAuthnProperties appProperties) {

        RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
                .id(appProperties.getRelyingPartyId()).name(appProperties.getRelyingPartyName())
                .icon(Optional.ofNullable(appProperties.getRelyingPartyIcon())).build();

        RelyingParty.RelyingPartyBuilder builder = RelyingParty.builder().identity(rpIdentity)
                .credentialRepository(credentialRepository)
                .origins(appProperties.getRelyingPartyOrigins());
        if (appProperties.getPreferredPubkeyParams() != null) {
            builder.preferredPubkeyParams(appProperties.getPreferredPubkeyParams());
        }
        return builder
                .build();
    }
}
