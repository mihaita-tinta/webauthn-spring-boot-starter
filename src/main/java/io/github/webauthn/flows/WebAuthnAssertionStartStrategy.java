package io.github.webauthn.flows;

import io.github.webauthn.config.WebAuthnOperation;
import io.github.webauthn.dto.AssertionStartRequest;
import io.github.webauthn.dto.AssertionStartResponse;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Base64;

public class WebAuthnAssertionStartStrategy {
    private static final Logger log = LoggerFactory.getLogger(WebAuthnAssertionStartStrategy.class);
    private final SecureRandom random = new SecureRandom();
    private final RelyingParty relyingParty;
    private final WebAuthnOperation<AssertionStartResponse, String> operation;

    public WebAuthnAssertionStartStrategy(RelyingParty relyingParty, WebAuthnOperation<AssertionStartResponse, String> operation) {
        this.relyingParty = relyingParty;
        this.operation = operation;
    }

    public AssertionStartResponse start(AssertionStartRequest request) {
        log.debug("start - {}", request);
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
