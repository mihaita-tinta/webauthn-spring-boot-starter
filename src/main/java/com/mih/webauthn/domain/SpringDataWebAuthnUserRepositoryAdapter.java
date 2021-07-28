package com.mih.webauthn.domain;

import java.time.LocalDateTime;
import java.util.Optional;

public class SpringDataWebAuthnUserRepositoryAdapter implements WebAuthnUserRepository{

    private final WebAuthnUserSpringDataRepository webAuthnUserSpringDataRepository;

    public SpringDataWebAuthnUserRepositoryAdapter(WebAuthnUserSpringDataRepository webAuthnUserSpringDataRepository) {
        this.webAuthnUserSpringDataRepository = webAuthnUserSpringDataRepository;
    }

    @Override
    public WebAuthnDefaultUser save(WebAuthnDefaultUser user) {
        return webAuthnUserSpringDataRepository.save(user);
    }

    @Override
    public Optional<WebAuthnDefaultUser> findById(Long id) {
        return webAuthnUserSpringDataRepository.findById(id);
    }

    @Override
    public Optional<WebAuthnDefaultUser> findByUsername(String username) {
        return webAuthnUserSpringDataRepository.findByUsername(username);
    }

    @Override
    public Optional<WebAuthnDefaultUser> findByAddTokenAndRegistrationAddStartAfter(byte[] token, LocalDateTime after) {
        return webAuthnUserSpringDataRepository.findByAddTokenAndRegistrationAddStartAfter(token, after);
    }

    @Override
    public Optional<WebAuthnDefaultUser> findByRecoveryToken(byte[] token) {
        return webAuthnUserSpringDataRepository.findByRecoveryToken(token);
    }
}
