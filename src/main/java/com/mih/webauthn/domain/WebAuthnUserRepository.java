package com.mih.webauthn.domain;

import java.time.LocalDateTime;
import java.util.Optional;

public interface WebAuthnUserRepository {

    WebAuthnUser save(WebAuthnUser user);

    Optional<WebAuthnUser> findById(Long id);

    Optional<WebAuthnUser> findByUsername(String username);

    Optional<WebAuthnUser> findByAddTokenAndRegistrationAddStartAfter(byte[] token, LocalDateTime after);

    Optional<WebAuthnUser> findByRecoveryToken(byte[] token);

    void deleteById(Long id);
}
