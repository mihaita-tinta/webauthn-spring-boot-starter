package com.mih.webauthn.domain;

import com.mih.webauthn.config.InMemoryOperation;
import com.mih.webauthn.config.WebAuthnOperation;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class WebAuthnUserInMemoryRepository implements WebAuthnUserRepository {
    private final WebAuthnOperation<WebAuthnUser, Long> users = new InMemoryOperation<>();
    private final AtomicLong COUNTER = new AtomicLong();

    @Override
    public WebAuthnUser save(WebAuthnUser user) {

        if (user.getId() == null) {
            user.setId(COUNTER.incrementAndGet());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<WebAuthnUser> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<WebAuthnUser> findByUsername(String username) {
        return users.list()
                .filter(u -> Objects.equals(u.getUsername(), username))
                .findFirst();
    }

    @Override
    public Optional<WebAuthnUser> findByAddTokenAndRegistrationAddStartAfter(byte[] token, LocalDateTime after) {
        return users.list()
                .filter(u -> Arrays.equals(u.getAddToken(), token) &&
                        (u.getRegistrationAddStart() != null && u.getRegistrationAddStart().isAfter(after)))
                .findFirst();
    }

    @Override
    public Optional<WebAuthnUser> findByRecoveryToken(byte[] token) {
        return users.list()
                .filter(u -> Arrays.equals(u.getRecoveryToken(), token))
                .findFirst();
    }

    @Override
    public void deleteById(Long id) {
        users.remove(id);
    }
}
