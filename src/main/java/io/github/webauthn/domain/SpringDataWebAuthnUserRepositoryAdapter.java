package io.github.webauthn.domain;

import java.time.LocalDateTime;
import java.util.Optional;

public class SpringDataWebAuthnUserRepositoryAdapter implements WebAuthnUserRepository{

    private final WebAuthnUserSpringDataRepository webAuthnUserSpringDataRepository;

    public SpringDataWebAuthnUserRepositoryAdapter(WebAuthnUserSpringDataRepository webAuthnUserSpringDataRepository) {
        this.webAuthnUserSpringDataRepository = webAuthnUserSpringDataRepository;
    }

    @Override
    public WebAuthnUser save(WebAuthnUser user) {
        return webAuthnUserSpringDataRepository.save(user);
    }

    @Override
    public Optional<WebAuthnUser> findById(Long id) {
        return webAuthnUserSpringDataRepository.findById(id);
    }

    @Override
    public Optional<WebAuthnUser> findByUsername(String username) {
        return webAuthnUserSpringDataRepository.findByUsername(username);
    }

    @Override
    public Optional<WebAuthnUser> findByAddTokenAndRegistrationAddStartAfter(byte[] token, LocalDateTime after) {
        return webAuthnUserSpringDataRepository.findByAddTokenAndRegistrationAddStartAfter(token, after);
    }

    @Override
    public Optional<WebAuthnUser> findByRecoveryToken(byte[] token) {
        return webAuthnUserSpringDataRepository.findByRecoveryToken(token);
    }

    @Override
    public void deleteById(Long id) {
        webAuthnUserSpringDataRepository.deleteById(id);
    }
}
