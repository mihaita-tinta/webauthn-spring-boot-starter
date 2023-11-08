package io.github.webauthn.domain;

import io.github.webauthn.config.InMemoryOperation;
import io.github.webauthn.config.WebAuthnOperation;
import io.github.webauthn.dto.RegistrationStartRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class WebAuthnUserInMemoryRepository implements WebAuthnUserRepository<DefaultWebAuthnUser> {
    private static final Logger log = LoggerFactory.getLogger(WebAuthnUserInMemoryRepository.class);
    private final WebAuthnOperation<DefaultWebAuthnUser, Long> users = new InMemoryOperation<>();
    private final AtomicLong COUNTER = new AtomicLong();

    @Override
    public DefaultWebAuthnUser save(DefaultWebAuthnUser user) {
        log.debug("save - {}", user);

        if (user.getId() == null) {
            user.setId(COUNTER.incrementAndGet());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<DefaultWebAuthnUser> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<DefaultWebAuthnUser> findByUsername(String username) {
        return users.list()
                .filter(u -> Objects.equals(u.getUsername(), username))
                .findFirst();
    }

    @Override
    public Optional<DefaultWebAuthnUser> findByAddTokenAndRegistrationAddStartAfter(byte[] token, LocalDateTime after) {
        return users.list()
                .filter(u -> Arrays.equals(u.getAddToken(), token) &&
                        (u.getRegistrationAddStart() != null && u.getRegistrationAddStart().isAfter(after)))
                .findFirst();
    }

    @Override
    public Optional<DefaultWebAuthnUser> findByRecoveryToken(byte[] token) {
        log.debug("findByRecoveryToken - {}", token);
        return users.list()
                .filter(u -> Arrays.equals(u.getRecoveryToken(), token))
                .findFirst();
    }

    @Override
    public void deleteById(Long id) {
        log.debug("deleteById - {}", id);
        users.remove(id);
    }

    @Override
    public DefaultWebAuthnUser newUser(RegistrationStartRequest startRequest) {
        DefaultWebAuthnUser u = new DefaultWebAuthnUser();
        u.setUsername(startRequest.getUsername());
        u.setFirstName(startRequest.getFirstName());
        u.setLastName(startRequest.getLastName());
        return u;
    }
}
