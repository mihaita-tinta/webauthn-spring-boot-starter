package com.mih.webauthn.config;

import com.mih.webauthn.BytesUtil;
import com.mih.webauthn.domain.AppCredentials;
import com.mih.webauthn.repository.AppCredentialsRepository;
import com.mih.webauthn.repository.AppUserRepository;
import com.mih.webauthn.dto.RegistrationFinishRequest;
import com.mih.webauthn.dto.RegistrationStartResponse;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.exception.RegistrationFailedException;

import java.security.SecureRandom;
import java.util.Base64;

public class WebAuthnRegistrationFinishStrategy {

    private final AppUserRepository appUserRepository;
    private final AppCredentialsRepository credentialRepository;
    private final SecureRandom random = new SecureRandom();
    private final RelyingParty relyingParty;
    private final WebAuthnOperation<RegistrationStartResponse> registrationOperation;

    public WebAuthnRegistrationFinishStrategy(AppUserRepository appUserRepository, AppCredentialsRepository credentialRepository, RelyingParty relyingParty, WebAuthnOperation registrationOperation) {
        this.appUserRepository = appUserRepository;
        this.credentialRepository = credentialRepository;
        this.relyingParty = relyingParty;
        this.registrationOperation = registrationOperation;
    }

    public String registrationFinish(RegistrationFinishRequest finishRequest) {

        RegistrationStartResponse startResponse = this.registrationOperation
                .get(finishRequest.getRegistrationId());
        this.registrationOperation.remove(finishRequest.getRegistrationId());

        if (startResponse == null) {
            throw new IllegalStateException("call start before this");

        }
        try {
            RegistrationResult registrationResult = this.relyingParty
                    .finishRegistration(FinishRegistrationOptions.builder()
                            .request(startResponse.getPublicKeyCredentialCreationOptions())
                            .response(finishRequest.getCredential()).build());
            UserIdentity userIdentity = startResponse.getPublicKeyCredentialCreationOptions()
                    .getUser();

            long userId = BytesUtil.bytesToLong(userIdentity.getId().getBytes());

            AppCredentials credentials = new AppCredentials(registrationResult.getKeyId().getId().getBytes(),
                    userId, finishRequest.getCredential().getResponse().getParsedAuthenticatorData()
                    .getSignatureCounter(),
                    registrationResult.getPublicKeyCose().getBytes()
            );
            this.credentialRepository.save(credentials);

            if (startResponse.getMode() == RegistrationStartResponse.Mode.NEW
                    || startResponse.getMode() == RegistrationStartResponse.Mode.RECOVERY) {
                byte[] recoveryToken = new byte[16];
                this.random.nextBytes(recoveryToken);

                this.appUserRepository.findById(userId)
                        .ifPresent(u -> {
                            u.setRecoveryToken(recoveryToken);
                            appUserRepository.save(u);
                        });

                return Base64.getEncoder().encodeToString(recoveryToken);
            }

            appUserRepository.findById(userId)
                    .ifPresent(user -> {
                        user.setAddToken(null);
                        user.setRegistrationAddStart(null);
                        appUserRepository.save(user);
                    });
            return "OK";
        } catch (RegistrationFailedException e) {
            throw new IllegalStateException("Registration failed ", e);
        }

    }
}
