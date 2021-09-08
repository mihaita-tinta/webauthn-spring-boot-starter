package io.github.webauthn.flows;

import io.github.webauthn.domain.WebAuthnUser;
import io.github.webauthn.domain.WebAuthnUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

public class WebAuthnRegistrationAddStrategy {
    private static final Logger log = LoggerFactory.getLogger(WebAuthnRegistrationAddStrategy.class);

    private final WebAuthnUserRepository webAuthnUserRepository;
    private final SecureRandom random = new SecureRandom();

    public WebAuthnRegistrationAddStrategy(WebAuthnUserRepository webAuthnUserRepository) {
        this.webAuthnUserRepository = webAuthnUserRepository;
    }

    public Map<String, String> registrationAdd(WebAuthnUser user) {
        log.debug("registrationAdd - {}", user);
        Assert.notNull(user, "user cannot be null");

        byte[] addToken = new byte[16];
        this.random.nextBytes(addToken);

        this.webAuthnUserRepository.findById(user.getId())
                .map(u -> {
                    u.setAddToken(addToken);
                    u.setRegistrationAddStart(LocalDateTime.now());
                    return webAuthnUserRepository.save(u);
                })
                .orElseThrow();

        return Map.of("registrationAddToken", Base64.getEncoder().encodeToString(addToken));
    }
}
