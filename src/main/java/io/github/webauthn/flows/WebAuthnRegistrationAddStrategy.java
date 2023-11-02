package io.github.webauthn.flows;

import io.github.webauthn.domain.WebAuthnUser;
import io.github.webauthn.domain.WebAuthnUserRepository;
import io.github.webauthn.events.NewRequestToAddDeviceEvent;
import io.github.webauthn.events.WebAuthnEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.Assert;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

public class WebAuthnRegistrationAddStrategy {
    private static final Logger log = LoggerFactory.getLogger(WebAuthnRegistrationAddStrategy.class);

    private final WebAuthnUserRepository<WebAuthnUser> webAuthnUserRepository;
    private final SecureRandom random = new SecureRandom();
    private final WebAuthnEventPublisher eventPublisher;

    public WebAuthnRegistrationAddStrategy(WebAuthnUserRepository webAuthnUserRepository, WebAuthnEventPublisher eventPublisher) {
        this.webAuthnUserRepository = webAuthnUserRepository;
        this.eventPublisher = eventPublisher;
    }

    public Map<String, String> registrationAdd(WebAuthnUser user) {
        log.debug("registrationAdd - {}", user);
        Assert.notNull(user, "user cannot be null");

        byte[] addToken = new byte[16];
        this.random.nextBytes(addToken);

        WebAuthnUser dbUser = this.webAuthnUserRepository.findById(user.getId())
                .map(u -> {
                    u.setAddToken(addToken);
                    u.setRegistrationAddStart(LocalDateTime.now());
                    return webAuthnUserRepository.save(u);
                })
                .orElseThrow();
        this.eventPublisher.publishEvent(new NewRequestToAddDeviceEvent(dbUser));

        return Map.of("registrationAddToken", Base64.getEncoder().encodeToString(addToken));
    }
}
