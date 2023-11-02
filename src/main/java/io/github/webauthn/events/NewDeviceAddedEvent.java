package io.github.webauthn.events;

import io.github.webauthn.domain.WebAuthnCredentials;
import io.github.webauthn.domain.WebAuthnUser;

public record NewDeviceAddedEvent(WebAuthnUser user, WebAuthnCredentials credentials) {
}
