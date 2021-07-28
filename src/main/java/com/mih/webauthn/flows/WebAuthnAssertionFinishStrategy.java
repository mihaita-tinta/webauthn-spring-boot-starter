package com.mih.webauthn.flows;

import com.mih.webauthn.BytesUtil;
import com.mih.webauthn.config.WebAuthnOperation;
import com.mih.webauthn.domain.WebAuthnDefaultUser;
import com.mih.webauthn.domain.WebAuthnUser;
import com.mih.webauthn.dto.AssertionFinishRequest;
import com.mih.webauthn.dto.AssertionStartResponse;
import com.mih.webauthn.domain.WebAuthnCredentialsRepository;
import com.mih.webauthn.domain.WebAuthnUserRepository;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.exception.AssertionFailedException;

import java.util.Optional;

public class WebAuthnAssertionFinishStrategy {

    private final WebAuthnUserRepository webAuthnUserRepository;
    private final WebAuthnCredentialsRepository webAuthnCredentialsRepository;
    private final RelyingParty relyingParty;
    private final WebAuthnOperation<AssertionStartResponse, String> operation;

    public WebAuthnAssertionFinishStrategy(WebAuthnUserRepository webAuthnUserRepository, WebAuthnCredentialsRepository webAuthnCredentialsRepository, RelyingParty relyingParty, WebAuthnOperation<AssertionStartResponse, String> operation) {
        this.webAuthnUserRepository = webAuthnUserRepository;
        this.webAuthnCredentialsRepository = webAuthnCredentialsRepository;
        this.relyingParty = relyingParty;
        this.operation = operation;
    }

    public Optional<WebAuthnUser> finish(AssertionFinishRequest finishRequest) {

        AssertionStartResponse startResponse = this.operation
                .get(finishRequest.getAssertionId());
        this.operation.remove(finishRequest.getAssertionId());

        if (startResponse == null) {
            throw new IllegalStateException("call start before this");

        }
        try {
            AssertionResult result = this.relyingParty.finishAssertion(
                    FinishAssertionOptions.builder().request(startResponse.getAssertionRequest())
                            .response(finishRequest.getCredential()).build());

            if (result.isSuccess()) {

                System.out.println("updateSignatureCount: " + result.getUserHandle());

                long appUserId = BytesUtil.bytesToLong(result.getUserHandle().getBytes());
                byte[] credentialId = result.getCredentialId().getBytes();

                webAuthnCredentialsRepository.findByCredentialIdAndAppUserId(credentialId, appUserId)
                        .map(credential -> {
                            credential.setCount(result.getSignatureCount());
                            return webAuthnCredentialsRepository.save(credential);
                        })
                        .orElseThrow();


                long userId = BytesUtil.bytesToLong(result.getUserHandle().getBytes());
                return this.webAuthnUserRepository.findById(userId)
                        .map(s -> s);
            }
        } catch (AssertionFailedException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }


        return Optional.empty();
    }
}
