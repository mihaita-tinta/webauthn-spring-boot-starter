package com.mih.webauthn;


import com.mih.webauthn.domain.WebAuthnCredentialsInMemoryRepository;
import com.mih.webauthn.domain.WebAuthnCredentialsRepository;
import com.mih.webauthn.domain.WebAuthnUserInMemoryRepository;
import com.mih.webauthn.domain.WebAuthnUserRepository;
import com.mih.webauthn.service.DefaultCredentialService;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
@EnableConfigurationProperties(WebAuthnProperties.class)
public class WebAuthnConfig {

    @Bean
    @ConditionalOnMissingBean(WebAuthnCredentialsRepository.class)
    public WebAuthnCredentialsRepository webAuthnCredentialsRepository() {
        return new WebAuthnCredentialsInMemoryRepository();
    }

    @Bean
    @ConditionalOnMissingBean(WebAuthnUserRepository.class)
    public WebAuthnUserRepository webAuthnUserRepository() {
        return new WebAuthnUserInMemoryRepository();
    }

    @Bean
    public CredentialRepository credentialRepositoryService() {
        return new DefaultCredentialService(webAuthnCredentialsRepository(), webAuthnUserRepository());
    }

    @Bean
    public RelyingParty relyingParty(CredentialRepository credentialRepository,
                                     WebAuthnProperties appProperties) {

        RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
                .id(appProperties.getRelyingPartyId()).name(appProperties.getRelyingPartyName())
                .icon(Optional.ofNullable(appProperties.getRelyingPartyIcon())).build();

        return RelyingParty.builder().identity(rpIdentity)
                .credentialRepository(credentialRepository)
                .origins(appProperties.getRelyingPartyOrigins()).build();
    }
}
