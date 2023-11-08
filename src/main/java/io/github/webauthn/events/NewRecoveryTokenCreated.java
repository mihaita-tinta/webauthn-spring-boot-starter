package io.github.webauthn.events;

import io.github.webauthn.domain.WebAuthnCredentials;
import io.github.webauthn.domain.WebAuthnUser;

public record NewRecoveryTokenCreated(WebAuthnUser user, WebAuthnCredentials credentials) {
}
