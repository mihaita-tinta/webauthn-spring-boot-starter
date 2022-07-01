package io.github.webauthn.dto;

public class RegistrationStartRequest {

    private String username;
    private String registrationAddToken;
    private String recoveryToken;
    private String firstName;
    private String lastName;

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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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
