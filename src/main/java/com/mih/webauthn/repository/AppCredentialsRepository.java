package com.mih.webauthn.repository;

import com.mih.webauthn.domain.AppCredentials;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface AppCredentialsRepository extends CrudRepository<AppCredentials, Long> {

    List<AppCredentials> findAllByAppUserId(Long userId);

    Optional<AppCredentials> findByCredentialIdAndAppUserId(byte[] credentialId, Long userId);

    List<AppCredentials> findByCredentialId(byte[] credentialId);

    @Transactional
    void deleteByAppUserId(Long appUserId);
}
