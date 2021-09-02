package io.github.webauthn.domain;

import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface WebAuthnUserSpringDataRepository extends CrudRepository<WebAuthnUser, Long> {

    <T extends WebAuthnUser> Optional<T> findByUsername(String username);

    <T extends WebAuthnUser> Optional<T> findByAddTokenAndRegistrationAddStartAfter(byte[] token, LocalDateTime after);

    <T extends WebAuthnUser> Optional<T> findByRecoveryToken(byte[] token);
}
