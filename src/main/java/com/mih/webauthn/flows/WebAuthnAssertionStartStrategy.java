package com.mih.webauthn.flows;

import com.mih.webauthn.config.WebAuthnOperation;
import com.mih.webauthn.dto.AssertionStartRequest;
import com.mih.webauthn.dto.AssertionStartResponse;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;

import java.security.SecureRandom;
import java.util.Base64;

public class WebAuthnAssertionStartStrategy {

    private final SecureRandom random = new SecureRandom();
    private final RelyingParty relyingParty;
    private final WebAuthnOperation<AssertionStartResponse, String> operation;

    public WebAuthnAssertionStartStrategy(RelyingParty relyingParty, WebAuthnOperation<AssertionStartResponse, String> operation) {
        this.relyingParty = relyingParty;
        this.operation = operation;
    }

    public AssertionStartResponse start(AssertionStartRequest request) {
        byte[] assertionId = new byte[16];
        this.random.nextBytes(assertionId);

        String assertionIdBase64 = Base64.getEncoder().encodeToString(assertionId);
        AssertionRequest assertionRequest = this.relyingParty
                .startAssertion(StartAssertionOptions.builder()
                        .username(request.getUsername())
                        .build());

        AssertionStartResponse response = new AssertionStartResponse(assertionIdBase64,
                assertionRequest);

        this.operation.put(response.getAssertionId(), response);
        return response;
    }
}
