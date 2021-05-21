package com.mih.webauthn.config;

import com.mih.webauthn.BytesUtil;
import com.mih.webauthn.domain.AppUser;
import com.mih.webauthn.dto.AssertionFinishRequest;
import com.mih.webauthn.dto.AssertionStartResponse;
import com.mih.webauthn.repository.AppUserRepository;
import com.mih.webauthn.service.CredentialService;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.exception.AssertionFailedException;

import java.util.Optional;

public class WebAuthnAssertionFinishStrategy {

    private final AppUserRepository appUserRepository;
    private final CredentialService credentialRepositoryService;
    private final RelyingParty relyingParty;
    private final WebAuthnOperation<AssertionStartResponse> operation;

    public WebAuthnAssertionFinishStrategy(AppUserRepository appUserRepository, CredentialService credentialRepository, RelyingParty relyingParty, WebAuthnOperation<AssertionStartResponse> operation) {
        this.appUserRepository = appUserRepository;
        this.credentialRepositoryService = credentialRepository;
        this.relyingParty = relyingParty;
        this.operation = operation;
    }

    public Optional<AppUser> finish(AssertionFinishRequest finishRequest) {

        AssertionStartResponse startResponse = this.operation
                .get(finishRequest.getAssertionId());
        this.operation.remove(finishRequest.getAssertionId());

        try {
            AssertionResult result = this.relyingParty.finishAssertion(
                    FinishAssertionOptions.builder().request(startResponse.getAssertionRequest())
                            .response(finishRequest.getCredential()).build());

            if (result.isSuccess()) {
                this.credentialRepositoryService.updateSignatureCount(result);

                long userId = BytesUtil.bytesToLong(result.getUserHandle().getBytes());
                return this.appUserRepository.findById(userId);
            }
        } catch (AssertionFailedException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }


        return Optional.empty();
    }
}
