package com.mih.webauthn;


import com.mih.webauthn.config.InMemoryOperation;
import com.mih.webauthn.config.WebAuthnOperation;
import com.mih.webauthn.domain.WebAuthnCredentialsInMemoryRepository;
import com.mih.webauthn.domain.WebAuthnUserInMemoryRepository;
import com.mih.webauthn.dto.AssertionStartResponse;
import com.mih.webauthn.dto.RegistrationStartResponse;
import com.mih.webauthn.service.DefaultCredentialService;
import com.mih.webauthn.domain.WebAuthnCredentialsRepository;
import com.mih.webauthn.domain.WebAuthnUserRepository;
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

}
