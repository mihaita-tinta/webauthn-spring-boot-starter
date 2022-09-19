package io.github.webauthn.flows;

public class UserCreationDisabledException extends RuntimeException {

    public UserCreationDisabledException(String message) {
        super(message);
    }
}
