package com.mih.webauthn.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class MyUser implements WebAuthnUser {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    private String username;
    private byte[] recoveryToken;
    private byte[] addToken;
    private byte[] registrationAddTokenDecoded;
    private LocalDateTime registrationAddStart;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public byte[] getRecoveryToken() {
        return recoveryToken;
    }

    @Override
    public void setRecoveryToken(byte[] recoveryToken) {
        this.recoveryToken = recoveryToken;
    }

    @Override
    public byte[] getAddToken() {
        return addToken;
    }

    @Override
    public void setAddToken(byte[] addToken) {
        this.addToken = addToken;
    }

    public byte[] getRegistrationAddTokenDecoded() {
        return registrationAddTokenDecoded;
    }

    public void setRegistrationAddTokenDecoded(byte[] registrationAddTokenDecoded) {
        this.registrationAddTokenDecoded = registrationAddTokenDecoded;
    }

    @Override
    public LocalDateTime getRegistrationAddStart() {
        return registrationAddStart;
    }

    @Override
    public void setRegistrationAddStart(LocalDateTime registrationAddStart) {
        this.registrationAddStart = registrationAddStart;
    }
}
