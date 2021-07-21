package com.mih.webauthn.dto;

public class RegistrationStartRequest {

    private String username;
    private String registrationAddToken;
    private String recoveryToken;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRegistrationAddToken() {
        return registrationAddToken;
    }

    public void setRegistrationAddToken(String registrationAddToken) {
        this.registrationAddToken = registrationAddToken;
    }

    public String getRecoveryToken() {
        return recoveryToken;
    }

    public void setRecoveryToken(String recoveryToken) {
        this.recoveryToken = recoveryToken;
    }

    @Override
    public String toString() {
        return "RegistrationStartRequest{" +
                "username='" + username + '\'' +
                ", registrationAddToken='" + registrationAddToken + '\'' +
                ", recoveryToken='" + recoveryToken + '\'' +
                '}';
    }
}
