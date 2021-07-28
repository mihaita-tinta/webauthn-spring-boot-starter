package com.mih.webauthn.domain;


import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface WebAuthnCredentialsSpringDataRepository extends CrudRepository<WebAuthnDefaultCredentials, Long> {

    List<WebAuthnCredentials> findAllByAppUserId(Long userId);

    Optional<WebAuthnCredentials> findByCredentialIdAndAppUserId(byte[] credentialId, Long userId);

    List<WebAuthnCredentials> findByCredentialId(byte[] credentialId);

    WebAuthnCredentials save(WebAuthnCredentials credentials);

    void deleteByAppUserId(Long appUserId);

    void deleteByCredentialId(byte[] credentialId);
}
