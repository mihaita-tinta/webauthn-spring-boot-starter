package com.mih.webauthn.flows;

import com.mih.webauthn.BytesUtil;
import com.mih.webauthn.config.WebAuthnOperation;
import com.mih.webauthn.domain.WebAuthnCredentials;
import com.mih.webauthn.domain.WebAuthnUser;
import com.mih.webauthn.dto.RegistrationFinishRequest;
import com.mih.webauthn.dto.RegistrationStartResponse;
import com.mih.webauthn.domain.WebAuthnCredentialsRepository;
import com.mih.webauthn.domain.WebAuthnUserRepository;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.exception.RegistrationFailedException;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.util.function.Consumer;

public class WebAuthnRegistrationFinishStrategy {

    private final WebAuthnUserRepository webAuthnUserRepository;
    private final WebAuthnCredentialsRepository credentialRepository;
    private final SecureRandom random = new SecureRandom();
    private final RelyingParty relyingParty;
    private final WebAuthnOperation<RegistrationStartResponse, String> registrationOperation;
    private Optional<Consumer<WebAuthnUser>> registerSuccessHandler;

    public WebAuthnRegistrationFinishStrategy(WebAuthnUserRepository webAuthnUserRepository, WebAuthnCredentialsRepository credentialRepository, RelyingParty relyingParty, WebAuthnOperation registrationOperation) {
        this.webAuthnUserRepository = webAuthnUserRepository;
        this.credentialRepository = credentialRepository;
        this.relyingParty = relyingParty;
        this.registrationOperation = registrationOperation;
        this.registerSuccessHandler = Optional.empty();
    }

    public Optional<Consumer<WebAuthnUser>> getRegisterSuccessHandler() {
        return registerSuccessHandler;
    }

    public void setRegisterSuccessHandler(Consumer<WebAuthnUser> registerSuccessHandler) {
        this.registerSuccessHandler = Optional.of(registerSuccessHandler);
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

            WebAuthnCredentials credentials = new WebAuthnCredentials(registrationResult.getKeyId().getId().getBytes(),
                    userId, finishRequest.getCredential().getResponse().getParsedAuthenticatorData()
                    .getSignatureCounter(),
                    registrationResult.getPublicKeyCose().getBytes()
            );
            this.credentialRepository.save(credentials);

            if (startResponse.getMode() == RegistrationStartResponse.Mode.NEW
                    || startResponse.getMode() == RegistrationStartResponse.Mode.RECOVERY) {
                byte[] recoveryToken = new byte[16];
                this.random.nextBytes(recoveryToken);

                this.webAuthnUserRepository.findById(userId)
                        .ifPresent(u -> {
                            u.setRecoveryToken(recoveryToken);
                            WebAuthnUser saved = webAuthnUserRepository.save(u);

                            registerSuccessHandler.ifPresent(reg -> reg.accept(saved));
                        });

                return Base64.getEncoder().encodeToString(recoveryToken);
            }

            webAuthnUserRepository.findById(userId)
                    .ifPresent(user -> {
                        user.setAddToken(null);
                        user.setRegistrationAddStart(null);
                        webAuthnUserRepository.save(user);
                    });
            return "OK";
        } catch (RegistrationFailedException e) {
            throw new IllegalStateException("Registration failed ", e);
        }

    }
}
