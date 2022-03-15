package io.github.webauthn.domain;

import io.github.webauthn.dto.RegistrationStartRequest;

import java.time.LocalDateTime;
import java.util.Optional;

public interface WebAuthnUserRepository<T extends WebAuthnUser> {

    T save(T user);

    Optional<T> findById(Long id);

    Optional<T> findByUsername(String username);

    Optional<T> findByAddTokenAndRegistrationAddStartAfter(byte[] token, LocalDateTime after);

    Optional<T> findByRecoveryToken(byte[] token);

    void deleteById(Long id);

    T newUser(RegistrationStartRequest startRequest);
}
