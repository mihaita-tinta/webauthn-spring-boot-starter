package com.mih.webauthn.flows;

import com.mih.webauthn.BytesUtil;
import com.mih.webauthn.WebAuthnFilter;
import com.mih.webauthn.config.WebAuthnOperation;
import com.mih.webauthn.domain.WebAuthnCredentialsRepository;
import com.mih.webauthn.domain.WebAuthnUser;
import com.mih.webauthn.domain.WebAuthnUserRepository;
import com.mih.webauthn.dto.RegistrationStartRequest;
import com.mih.webauthn.dto.RegistrationStartResponse;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.UserIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import static org.springframework.util.StringUtils.hasText;

public class WebAuthnRegistrationStartStrategy {
    private static final Logger log = LoggerFactory.getLogger(WebAuthnRegistrationStartStrategy.class);

    private final WebAuthnUserRepository webAuthnUserRepository;
    private final WebAuthnCredentialsRepository webAuthnCredentialRepository;
    private final SecureRandom random = new SecureRandom();
    private final RelyingParty relyingParty;
    private final WebAuthnOperation registrationOperation;

    public WebAuthnRegistrationStartStrategy(WebAuthnUserRepository webAuthnUserRepository, WebAuthnCredentialsRepository webAuthnCredentialRepository, RelyingParty relyingParty, WebAuthnOperation registrationOperation) {
        this.webAuthnUserRepository = webAuthnUserRepository;
        this.webAuthnCredentialRepository = webAuthnCredentialRepository;
        this.relyingParty = relyingParty;
        this.registrationOperation = registrationOperation;
    }

    public RegistrationStartResponse registrationStart(RegistrationStartRequest request) {
        log.debug("registrationStart - request: {}", request);

        long userId = -1;
        String name = null;
        RegistrationStartResponse.Mode mode = null;

        if (hasText(request.getUsername())) {
            this.webAuthnUserRepository.findByUsername(request.getUsername())
                    .ifPresent(u -> {
                        throw new UsernameAlreadyExistsException("Username taken");
                    });

            WebAuthnUser user = new WebAuthnUser();
            user.setUsername(request.getUsername());
            userId = this.webAuthnUserRepository.save(user)
                    .getId();
            name = request.getUsername();
            mode = RegistrationStartResponse.Mode.NEW;
        } else if (request.getRegistrationAddToken() != null && !request.getRegistrationAddToken().isEmpty()) {
            byte[] registrationAddTokenDecoded = null;
            try {
                registrationAddTokenDecoded = Base64.getDecoder().decode(request.getRegistrationAddToken());
            } catch (Exception e) {
                throw new InvalidTokenException("Registration Add Token invalid");
            }

            WebAuthnUser user = webAuthnUserRepository.findByAddTokenAndRegistrationAddStartAfter(
                    registrationAddTokenDecoded, LocalDateTime.now().minusMinutes(10))
                    .orElseThrow(() -> new InvalidTokenException("Registration Add Token expired"));


            userId = user.getId();
            name = user.getUsername();
            mode = RegistrationStartResponse.Mode.ADD;
        } else if (request.getRecoveryToken() != null && !request.getRecoveryToken().isEmpty()) {
            byte[] recoveryTokenDecoded = null;
            try {
                recoveryTokenDecoded = Base64.getDecoder().decode(request.getRecoveryToken());
            } catch (Exception e) {
                throw new InvalidTokenException("Recovery Token invalid");
            }
            WebAuthnUser user = webAuthnUserRepository.findByRecoveryToken(recoveryTokenDecoded)
                    .orElseThrow(() -> new InvalidTokenException("Recovery token not found"));

            userId = user.getId();
            name = user.getUsername();
            mode = RegistrationStartResponse.Mode.RECOVERY;
            webAuthnCredentialRepository.deleteByAppUserId(userId);
        }

        if (mode != null) {
            PublicKeyCredentialCreationOptions credentialCreation = this.relyingParty
                    .startRegistration(StartRegistrationOptions.builder()
                            .user(UserIdentity
                                    .builder()
                                    .name(name)
                                    .displayName(name)
                                    .id(new ByteArray(BytesUtil.longToBytes(userId))).build())
                            .build());

            byte[] registrationId = new byte[16];
            this.random.nextBytes(registrationId);
            RegistrationStartResponse startResponse = new RegistrationStartResponse(mode,
                    Base64.getEncoder().encodeToString(registrationId), credentialCreation);

            registrationOperation.put(startResponse.getRegistrationId(), startResponse);

            return startResponse;
        }
        return null;
    }
}
