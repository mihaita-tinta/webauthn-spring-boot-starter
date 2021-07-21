package com.mih.webauthn.domain;


import com.mih.webauthn.config.InMemoryOperation;
import com.mih.webauthn.config.WebAuthnOperation;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public class WebAuthnCredentialsInMemoryRepository implements WebAuthnCredentialsRepository {
    private final WebAuthnOperation<List<WebAuthnCredentials>, Long> credentialsByUserId = new InMemoryOperation<>();

    @Override
    public List<WebAuthnCredentials> findAllByAppUserId(Long userId) {
        return Optional.ofNullable(credentialsByUserId.get(userId))
                .orElse(emptyList());
    }

    @Override
    public Optional<WebAuthnCredentials> findByCredentialIdAndAppUserId(byte[] credentialId, Long userId) {
        return credentialsByUserId.list()
                .flatMap(l -> l.stream().filter(c -> c.getAppUserId().equals(userId) &&
                        Arrays.equals(c.getCredentialId(), credentialId)))
                .findFirst();
    }

    @Override
    public List<WebAuthnCredentials> findByCredentialId(byte[] credentialId) {
        return credentialsByUserId.list()
                .flatMap(l -> l.stream().filter(c -> Arrays.equals(c.getCredentialId(), credentialId)))
                .collect(Collectors.toList());
    }

    @Override
    public WebAuthnCredentials save(WebAuthnCredentials credentials) {
        List<WebAuthnCredentials> list = credentialsByUserId.get(credentials.getAppUserId());
        if (list == null) {
            list = new ArrayList<>();
            credentialsByUserId.put(credentials.getAppUserId(), list);
        }
        list.add(credentials);
        return credentials;
    }

    @Override
    public void deleteByAppUserId(Long appUserId) {
        credentialsByUserId.remove(appUserId);
    }

}
