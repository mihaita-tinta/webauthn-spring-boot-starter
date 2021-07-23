package com.mih.webauthn.domain;

import java.time.LocalDateTime;
public class WebAuthnUser {

    private Long id;
    private String username;
    private byte[] recoveryToken;
    private byte[] addToken;
    private LocalDateTime registrationAddStart;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte[] getRecoveryToken() {
        return recoveryToken;
    }

    public void setRecoveryToken(byte[] recoveryToken) {
        this.recoveryToken = recoveryToken;
    }

    public byte[] getAddToken() {
        return addToken;
    }

    public void setAddToken(byte[] addToken) {
        this.addToken = addToken;
    }

    public LocalDateTime getRegistrationAddStart() {
        return registrationAddStart;
    }

    public void setRegistrationAddStart(LocalDateTime registrationAddStart) {
        this.registrationAddStart = registrationAddStart;
    }
}
