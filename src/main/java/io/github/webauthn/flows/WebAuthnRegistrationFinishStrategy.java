package io.github.webauthn.flows;

import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.exception.RegistrationFailedException;
import io.github.webauthn.BytesUtil;
import io.github.webauthn.config.WebAuthnOperation;
import io.github.webauthn.domain.*;
import io.github.webauthn.dto.RegistrationFinishRequest;
import io.github.webauthn.dto.RegistrationStartResponse;
import io.github.webauthn.events.NewRecoveryTokenCreated;
import io.github.webauthn.events.WebAuthnEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

public class WebAuthnRegistrationFinishStrategy {

    private static final Logger log = LoggerFactory.getLogger(WebAuthnRegistrationFinishStrategy.class);
    private final WebAuthnUserRepository<WebAuthnUser> webAuthnUserRepository;
    private final WebAuthnCredentialsRepository<WebAuthnCredentials> credentialRepository;
    private final SecureRandom random = new SecureRandom();
    private final RelyingParty relyingParty;
    private final WebAuthnOperation<RegistrationStartResponse, String> registrationOperation;
    private final WebAuthnEventPublisher publisher;

    public WebAuthnRegistrationFinishStrategy(WebAuthnUserRepository webAuthnUserRepository, WebAuthnCredentialsRepository credentialRepository,
                                              RelyingParty relyingParty, WebAuthnOperation registrationOperation,
                                              WebAuthnEventPublisher publisher) {
        this.webAuthnUserRepository = webAuthnUserRepository;
        this.credentialRepository = credentialRepository;
        this.relyingParty = relyingParty;
        this.registrationOperation = registrationOperation;
        this.publisher = publisher;
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

            WebAuthnUser user = this.webAuthnUserRepository.findById(userId)
                    .orElseThrow();

            if (user.isEnabled() && startResponse.getMode() == RegistrationStartResponse.Mode.NEW) {
                throw new IllegalStateException("The user can only migrate his account to webauthn or use the recovery token");
            }

            WebAuthnCredentials newCredentials = this.credentialRepository.save(registrationResult.getKeyId().getId().getBytes(),
                    userId, finishRequest.getCredential().getResponse().getParsedAuthenticatorData()
                            .getSignatureCounter(),
                    registrationResult.getPublicKeyCose().getBytes(),
                    finishRequest.getUserAgent());

            if (startResponse.getMode() == RegistrationStartResponse.Mode.NEW) {
                user.setEnabled(true);
            }

            if (startResponse.getMode() == RegistrationStartResponse.Mode.NEW
                    || startResponse.getMode() == RegistrationStartResponse.Mode.RECOVERY
                    || startResponse.getMode() == RegistrationStartResponse.Mode.MIGRATE) {
                byte[] recoveryToken = new byte[16];
                this.random.nextBytes(recoveryToken);

                user.setRecoveryToken(recoveryToken);
                WebAuthnUser saved = webAuthnUserRepository.save(user);

                publisher.publish(new NewRecoveryTokenCreated(saved, newCredentials));

                return Map.of("recoveryToken", Base64.getEncoder().encodeToString(recoveryToken));
            }

            user.setAddToken(null);
            user.setRegistrationAddStart(null);
            webAuthnUserRepository.save(user);
            return Collections.emptyMap();
        } catch (RegistrationFailedException e) {
            throw new IllegalStateException("Registration failed ", e);
        }

    }
}
