package com.mih.webauthn.domain;

import java.time.LocalDateTime;
import java.util.Optional;

public interface WebAuthnUserRepository {

    WebAuthnDefaultUser save(WebAuthnDefaultUser user);

    Optional<WebAuthnDefaultUser> findById(Long id);

    Optional<WebAuthnDefaultUser> findByUsername(String username);

    Optional<WebAuthnDefaultUser> findByAddTokenAndRegistrationAddStartAfter(byte[] token, LocalDateTime after);

    Optional<WebAuthnDefaultUser> findByRecoveryToken(byte[] token);
}
