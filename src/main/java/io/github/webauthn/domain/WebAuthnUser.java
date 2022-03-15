package io.github.webauthn.domain;

import java.time.LocalDateTime;

public interface WebAuthnUser {

    Long getId();

    void setId(Long id);

    LocalDateTime getRegistrationAddStart();

    void setRegistrationAddStart(LocalDateTime registrationAddStart);

    String getUsername();

    byte[] getAddToken();

    void setAddToken(byte[] addToken);

    byte[] getRecoveryToken();

    void setRecoveryToken(byte[] recoveryToken);

    boolean isEnabled();

    void setEnabled(boolean enabled);

}
