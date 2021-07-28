package com.mih.webauthn;


import com.mih.webauthn.config.InMemoryOperation;
import com.mih.webauthn.config.WebAuthnOperation;
import com.mih.webauthn.domain.WebAuthnCredentialsInMemoryRepository;
import com.mih.webauthn.domain.WebAuthnCredentialsRepository;
import com.mih.webauthn.domain.WebAuthnUserInMemoryRepository;
import com.mih.webauthn.domain.WebAuthnUserRepository;
import com.mih.webauthn.dto.AssertionStartResponse;
import com.mih.webauthn.dto.RegistrationStartResponse;
import com.mih.webauthn.service.DefaultCredentialService;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
@EnableConfigurationProperties(WebAuthnProperties.class)
@AutoConfigureAfter(HibernateJpaAutoConfiguration.class)
public class WebAuthnConfig {

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

        return RelyingParty.builder().identity(rpIdentity)
                .credentialRepository(credentialRepository)
                .origins(appProperties.getRelyingPartyOrigins()).build();
    }
}
