package io.github.webauthn.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import io.github.webauthn.domain.WebAuthnCredentials;
import io.github.webauthn.domain.WebAuthnUser;
import io.github.webauthn.events.NewDeviceAddedEvent;
import io.github.webauthn.events.NewRecoveryTokenCreated;
import io.github.webauthn.events.NewUserCreatedEvent;
import io.github.webauthn.events.UserMigratedEvent;

public class RegistrationStartResponse {

    public Object getRegistrationEvent(WebAuthnUser user, WebAuthnCredentials credentials) {
      return switch (mode) {
        case NEW -> new NewUserCreatedEvent(user, credentials);
        case RECOVERY -> new NewRecoveryTokenCreated(user, credentials);
        case MIGRATE -> new UserMigratedEvent(user, credentials);
        case ADD -> new NewDeviceAddedEvent(user, credentials);
      };
    }

    public enum Status {
    OK, USERNAME_TAKEN, TOKEN_INVALID, USER_REGISTRATION_DISABLED
  }

  public enum Mode {
    NEW, ADD, RECOVERY, MIGRATE
  }

  @JsonIgnore
  private final Mode mode;

  private final Status status;

  private final String registrationId;

  private final PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions;

  public RegistrationStartResponse(Mode mode, String registrationId,
      PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions) {
    this.mode = mode;
    this.status = Status.OK;
    this.registrationId = registrationId;
    this.publicKeyCredentialCreationOptions = publicKeyCredentialCreationOptions;
  }

  public RegistrationStartResponse(Status status) {
    this.mode = null;
    this.status = status;
    this.registrationId = null;
    this.publicKeyCredentialCreationOptions = null;
  }

  public Status getStatus() {
    return this.status;
  }

  public String getRegistrationId() {
    return this.registrationId;
  }

  public PublicKeyCredentialCreationOptions getPublicKeyCredentialCreationOptions() {
    return this.publicKeyCredentialCreationOptions;
  }

  public Mode getMode() {
    return this.mode;
  }

}
