package com.mih.webauthn.config;

import com.mih.webauthn.dto.AssertionStartResponse;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import org.springframework.web.bind.annotation.RequestBody;

import java.security.SecureRandom;
import java.util.Base64;

public class WebAuthnAssertionStartStrategy {

    private final SecureRandom random = new SecureRandom();
    private final RelyingParty relyingParty;
    private final WebAuthnOperation<AssertionStartResponse> operation;

    public WebAuthnAssertionStartStrategy(RelyingParty relyingParty, WebAuthnOperation<AssertionStartResponse> operation) {
        this.relyingParty = relyingParty;
        this.operation = operation;
    }

    public AssertionStartResponse start(@RequestBody String username) {
        byte[] assertionId = new byte[16];
        this.random.nextBytes(assertionId);

        String assertionIdBase64 = Base64.getEncoder().encodeToString(assertionId);
        AssertionRequest assertionRequest = this.relyingParty
                .startAssertion(StartAssertionOptions.builder().username(username).build());

        AssertionStartResponse response = new AssertionStartResponse(assertionIdBase64,
                assertionRequest);

        this.operation.put(response.getAssertionId(), response);
        return response;
    }
}
