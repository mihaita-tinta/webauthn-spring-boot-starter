package io.github.webauthn.flows;

import com.yubico.webauthn.exception.AssertionFailedException;

public class WebAuthnAssertionFailedException extends RuntimeException {

    public WebAuthnAssertionFailedException(AssertionFailedException e) {
        super(e.getMessage(), e);
    }
}
