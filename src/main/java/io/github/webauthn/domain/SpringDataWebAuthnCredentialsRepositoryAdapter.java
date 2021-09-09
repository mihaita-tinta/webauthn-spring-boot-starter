package io.github.webauthn.domain;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
public class SpringDataWebAuthnCredentialsRepositoryAdapter implements WebAuthnCredentialsRepository {

    private final WebAuthnCredentialsSpringDataRepository webAuthnCredentialsRepository;


    public SpringDataWebAuthnCredentialsRepositoryAdapter(WebAuthnCredentialsSpringDataRepository webAuthnCredentialsRepository) {
        this.webAuthnCredentialsRepository = webAuthnCredentialsRepository;
    }

    @Override
    public List<WebAuthnCredentials> findAllByAppUserId(Long userId) {
        return webAuthnCredentialsRepository.findAllByAppUserId(userId);
    }

    @Override
    public Optional<WebAuthnCredentials> findByCredentialIdAndAppUserId(byte[] credentialId, Long userId) {
        return webAuthnCredentialsRepository.findByCredentialIdAndAppUserId(credentialId, userId);
    }

    @Override
    public List<WebAuthnCredentials> findByCredentialId(byte[] credentialId) {
        return webAuthnCredentialsRepository.findByCredentialId(credentialId);
    }

    @Override
    public WebAuthnCredentials save(WebAuthnCredentials credentials) {
        return webAuthnCredentialsRepository.save(credentials);
    }

    @Override
    public void deleteByAppUserId(Long appUserId) {
        webAuthnCredentialsRepository.deleteByAppUserId(appUserId);
    }

    @Override
    public void deleteById(Long id) {
        this.webAuthnCredentialsRepository.deleteById(id);
    }
}
