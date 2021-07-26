package com.mih.webauthn.domain;

import java.time.LocalDateTime;
import java.util.Optional;

public interface WebAuthnUserRepository<T extends WebAuthnUser> {

    T save(T user);

    Optional<T> findById(Long id);

    Optional<T> findByUsername(String username);

    Optional<T> findByAddTokenAndRegistrationAddStartAfter(byte[] token, LocalDateTime after);

    Optional<T> findByRecoveryToken(byte[] token);
}
