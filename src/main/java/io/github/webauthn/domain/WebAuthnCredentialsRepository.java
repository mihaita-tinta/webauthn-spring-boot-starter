package io.github.webauthn.domain;


import io.github.webauthn.dto.RegistrationStartRequest;

import java.util.List;
import java.util.Optional;

public interface WebAuthnCredentialsRepository<T extends WebAuthnCredentials> {

    List<T> findAllByAppUserId(Long userId);

    Optional<T> findByCredentialIdAndAppUserId(byte[] credentialId, Long userId);

    List<T> findByCredentialId(byte[] credentialId);

    T save(T credentials);

    void deleteByAppUserId(Long appUserId);

    void deleteById(Long id);

    T save(byte[] credentialId, Long appUserId, Long count, byte[] publicKeyCose, String userAgent);
}
