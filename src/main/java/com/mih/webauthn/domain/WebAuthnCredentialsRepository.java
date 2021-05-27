package com.mih.webauthn.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface WebAuthnCredentialsRepository extends CrudRepository<WebAuthnCredentials, Long> {

    List<WebAuthnCredentials> findAllByAppUserId(Long userId);

    Optional<WebAuthnCredentials> findByCredentialIdAndAppUserId(byte[] credentialId, Long userId);

    List<WebAuthnCredentials> findByCredentialId(byte[] credentialId);

    @Transactional
    void deleteByAppUserId(Long appUserId);
}
