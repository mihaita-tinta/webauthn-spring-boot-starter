package io.github.webauthn.events;

import org.springframework.context.ApplicationEventPublisher;

public class WebAuthnEventPublisher {

    private final ApplicationEventPublisher publisher;

    public WebAuthnEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }


    public void publish(NewRecoveryTokenCreated event) {
        publisher.publishEvent(event);
    }
}
