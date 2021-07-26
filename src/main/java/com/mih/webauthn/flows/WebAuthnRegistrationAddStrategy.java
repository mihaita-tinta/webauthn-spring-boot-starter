package com.mih.webauthn.flows;

import com.mih.webauthn.domain.WebAuthnUser;
import com.mih.webauthn.domain.WebAuthnUserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

public class WebAuthnRegistrationAddStrategy {

    private final WebAuthnUserRepository<WebAuthnUser> webAuthnUserRepository;
    private final SecureRandom random = new SecureRandom();

    public WebAuthnRegistrationAddStrategy(WebAuthnUserRepository webAuthnUserRepository) {
        this.webAuthnUserRepository = webAuthnUserRepository;
    }

    public Map<String, String> registrationAdd(WebAuthnUser user) {

        byte[] addToken = new byte[16];
        this.random.nextBytes(addToken);

        this.webAuthnUserRepository.findById(user.getId())
                .map(u -> {
                    u.setAddToken(addToken);
                    u.setRegistrationAddStart(LocalDateTime.now());
                    return webAuthnUserRepository.save(u);
                })
                .orElseThrow();

        return Map.of("registrationAddToken", Base64.getEncoder().encodeToString(addToken));
    }
}
