package com.mih.webauthn;


import com.mih.webauthn.domain.WebAuthnCredentialsRepository;
import com.mih.webauthn.domain.WebAuthnUserRepository;
import com.mih.webauthn.service.DefaultCredentialService;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Optional;

@Configuration
@EnableJpaRepositories
@EntityScan("com.mih.webauthn.domain")
@EnableConfigurationProperties(WebAuthnProperties.class)
public class WebAuthnConfig {

    @Bean
    public CredentialRepository credentialRepositoryService(WebAuthnCredentialsRepository webAuthnCredentialsRepository, WebAuthnUserRepository webAuthnUserRepository) {
        return new DefaultCredentialService(webAuthnCredentialsRepository, webAuthnUserRepository);
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
