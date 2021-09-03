package io.github.webauthn.flows;

import io.github.webauthn.BytesUtil;
import io.github.webauthn.config.WebAuthnOperation;
import io.github.webauthn.domain.WebAuthnCredentials;
import io.github.webauthn.domain.WebAuthnCredentialsRepository;
import io.github.webauthn.domain.WebAuthnUser;
import io.github.webauthn.domain.WebAuthnUserRepository;
import io.github.webauthn.dto.RegistrationFinishRequest;
import io.github.webauthn.dto.RegistrationStartResponse;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.exception.RegistrationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class WebAuthnRegistrationFinishStrategy {

    private static final Logger log = LoggerFactory.getLogger(WebAuthnRegistrationFinishStrategy.class);
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
        this.registerSuccessHandler = Optional.ofNullable(registerSuccessHandler);
    }

    public Map<String, String> registrationFinish(RegistrationFinishRequest finishRequest) {
        log.debug("registrationFinish - {}", finishRequest);

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
                    registrationResult.getPublicKeyCose().getBytes(),
                    finishRequest.getUserAgent()
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

                return Map.of("recoveryToken", Base64.getEncoder().encodeToString(recoveryToken));
            }

            webAuthnUserRepository.findById(userId)
                    .ifPresent(user -> {
                        user.setAddToken(null);
                        user.setRegistrationAddStart(null);
                        webAuthnUserRepository.save(user);
                    });
            return Collections.emptyMap();
        } catch (RegistrationFailedException e) {
            throw new IllegalStateException("Registration failed ", e);
        }

    }
}
