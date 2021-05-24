package com.mih.webauthn.domain;

import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface WebAuthnUserRepository extends CrudRepository<WebAuthnUser, Long> {

    Optional<WebAuthnUser> findByUsername(String username);

    Optional<WebAuthnUser> findByAddTokenAndRegistrationAddStartAfter(byte[] token, LocalDateTime after);

    Optional<WebAuthnUser> findByRecoveryToken(byte[] token);
}
