package com.mih.webauthn.domain;

import java.time.LocalDateTime;

public interface WebAuthnUser {

    Long getId();

    void setId(Long id);

    String getUsername();

    void setUsername(String username);

    byte[] getRecoveryToken();

    void setRecoveryToken(byte[] recoveryToken);
    void setAddToken(byte[] addToken);
    void setRegistrationAddStart(LocalDateTime start);

    byte[] getAddToken();

    LocalDateTime getRegistrationAddStart();
}
