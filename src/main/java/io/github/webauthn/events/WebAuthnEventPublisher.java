package io.github.webauthn.events;

import org.springframework.context.ApplicationEventPublisher;

public class WebAuthnEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public WebAuthnEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publishEvent(Object event) {
        this.eventPublisher.publishEvent(event);
    }
}
