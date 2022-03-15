package io.github.webauthn.domain;


import io.github.webauthn.config.InMemoryOperation;
import io.github.webauthn.config.WebAuthnOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public class WebAuthnCredentialsInMemoryRepository implements WebAuthnCredentialsRepository {
    private static final Logger log = LoggerFactory.getLogger(WebAuthnCredentialsInMemoryRepository.class);
    private final WebAuthnOperation<List<WebAuthnCredentials>, Long> credentialsByUserId = new InMemoryOperation<>();
    private final AtomicLong COUNTER = new AtomicLong();

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
        log.debug("save - {}", credentials);

        if (credentials.getId() == null) {
            credentials.setId(COUNTER.incrementAndGet());
        }

        List<WebAuthnCredentials> list = credentialsByUserId.get(credentials.getAppUserId());
        if (list == null) {
            list = new ArrayList<>();
            credentialsByUserId.put(credentials.getAppUserId(), list);
        }
        if (!list.contains(credentials)) {
            list.add(credentials);
        }
        return credentials;
    }

    @Override
    public void deleteByAppUserId(Long appUserId) {
        log.debug("deleteByAppUserId - {}", appUserId);
        credentialsByUserId.remove(appUserId);
    }

    @Override
    public void deleteById(Long id) {
        log.debug("deleteById - {}", id);
        credentialsByUserId.list()
                .filter(list -> {
                    Optional<WebAuthnCredentials> any = list.stream()
                            .filter(c -> Objects.equals(c.getId(), id))
                            .findAny();
                    any
                            .ifPresent(c -> list.remove(c));
                    return any.isPresent();
                })
                .findFirst();
    }

    @Override
    public WebAuthnCredentials save(byte[] credentialId, Long appUserId, Long count, byte[] publicKeyCose, String userAgent) {

        return new DefaultWebAuthnCredentials(credentialId,
                appUserId, count, publicKeyCose,userAgent
        );
    }

}
