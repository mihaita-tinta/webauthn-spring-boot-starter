package com.mih.webauthn.config;

import com.mih.webauthn.domain.WebAuthnUser;
import com.mih.webauthn.domain.WebAuthnUserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

public class WebAuthnRegistrationAddStrategy {

    private final WebAuthnUserRepository webAuthnUserRepository;
    private final SecureRandom random = new SecureRandom();

    public WebAuthnRegistrationAddStrategy(WebAuthnUserRepository webAuthnUserRepository) {
        this.webAuthnUserRepository = webAuthnUserRepository;
    }

    public String registrationAdd(WebAuthnUser user) {

        byte[] addToken = new byte[16];
        this.random.nextBytes(addToken);

        this.webAuthnUserRepository.findById(user.getId())
                .map(u -> {
                    u.setAddToken(addToken);
                    u.setRegistrationAddStart(LocalDateTime.now());
                    return webAuthnUserRepository.save(u);
                })
                .orElseThrow();

        return Base64.getEncoder().encodeToString(addToken);
    }
}
