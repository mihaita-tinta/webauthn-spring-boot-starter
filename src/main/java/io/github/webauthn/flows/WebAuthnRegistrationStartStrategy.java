package io.github.webauthn.flows;

import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.*;
import io.github.webauthn.BytesUtil;
import io.github.webauthn.WebAuthnProperties;
import io.github.webauthn.config.WebAuthnOperation;
import io.github.webauthn.domain.*;
import io.github.webauthn.dto.RegistrationStartRequest;
import io.github.webauthn.dto.RegistrationStartResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.util.StringUtils.hasText;

public class WebAuthnRegistrationStartStrategy {
    private static final Logger log = LoggerFactory.getLogger(WebAuthnRegistrationStartStrategy.class);

    private final WebAuthnUserRepository<WebAuthnUser> webAuthnUserRepository;
    private final WebAuthnCredentialsRepository<WebAuthnCredentials> webAuthnCredentialRepository;
    private final SecureRandom random = new SecureRandom();
    private final RelyingParty relyingParty;
    private final WebAuthnOperation registrationOperation;
    private final WebAuthnProperties properties;

    public WebAuthnRegistrationStartStrategy(WebAuthnUserRepository webAuthnUserRepository, WebAuthnCredentialsRepository webAuthnCredentialRepository, RelyingParty relyingParty, WebAuthnOperation registrationOperation, WebAuthnProperties properties) {
        this.webAuthnUserRepository = webAuthnUserRepository;
        this.webAuthnCredentialRepository = webAuthnCredentialRepository;
        this.relyingParty = relyingParty;
        this.registrationOperation = registrationOperation;
        this.properties = properties;
    }

    public RegistrationStartResponse registrationStart(RegistrationStartRequest request, Optional<? extends WebAuthnUser> currentUser) {
        log.debug("registrationStart - {}", request);

        RegistrationStartResponse.Mode mode = null;

        WebAuthnUser user = null;
        if (currentUser.isPresent()) {
            user = currentUser.get();
            mode = RegistrationStartResponse.Mode.MIGRATE;
        } else if (StringUtils.hasLength(request.getRegistrationAddToken())) {
            byte[] registrationAddTokenDecoded = null;
            try {
                registrationAddTokenDecoded = Base64.getDecoder().decode(request.getRegistrationAddToken());
            } catch (Exception e) {
                throw new InvalidTokenException("Registration Add Token invalid");
            }

            user = webAuthnUserRepository.findByAddTokenAndRegistrationAddStartAfter(
                            registrationAddTokenDecoded, LocalDateTime.now().minusMinutes(10))
                    .orElseThrow(() -> new InvalidTokenException("Registration Add Token expired"));

            mode = RegistrationStartResponse.Mode.ADD;
        } else if (StringUtils.hasLength(request.getRecoveryToken())) {
            byte[] recoveryTokenDecoded = null;
            try {
                recoveryTokenDecoded = Base64.getDecoder().decode(request.getRecoveryToken());
            } catch (Exception e) {
                throw new InvalidTokenException("One of the fields username, registrationAddToken, recoveryToken should be added");
            }
            user = webAuthnUserRepository.findByRecoveryToken(recoveryTokenDecoded)
                    .orElseThrow(() -> new InvalidTokenException("Recovery token not found"));

            mode = RegistrationStartResponse.Mode.RECOVERY;
            webAuthnCredentialRepository.deleteByAppUserId(user.getId());
        } else {
            if (!properties.isRegistrationNewUsersEnabled()) {
                throw new UserCreationDisabledException("Registration for new users is disabled");
            }

            mode = RegistrationStartResponse.Mode.NEW;
            boolean usernameFound = hasText(request.getUsername());
            if (properties.isUsernameRequired() && !usernameFound) {
                throw new InvalidTokenException("Username required");
            }

            if (usernameFound) {
                user = this.webAuthnUserRepository.findByUsername(request.getUsername())
                        .map(u -> {
                            if (u.isEnabled())
                                throw new UsernameAlreadyExistsException("Username taken");
                            // FIXME address race condition
                            return u;
                        })
                        .orElseGet(() -> this.webAuthnUserRepository.save(webAuthnUserRepository.newUser(request)));
            } else {
                request.setUsername(UUID.randomUUID().toString());
                WebAuthnUser requestUser = webAuthnUserRepository.newUser(request);
                user = this.webAuthnUserRepository.save(requestUser);
            }
        }

        if (mode == null) {
            new InvalidTokenException("Username required");
        }

        PublicKeyCredentialCreationOptions credentialCreation = this.relyingParty
                .startRegistration(StartRegistrationOptions.builder()
                        .user(UserIdentity
                                .builder()
                                .name(user.getUsername())
                                .displayName(getDisplayName(user))
                                .id(new ByteArray(BytesUtil.longToBytes(user.getId()))).build())
                        .authenticatorSelection(
                                AuthenticatorSelectionCriteria.builder()
                                        .residentKey(
                                                properties.isUsernameRequired()
                                                        ? ResidentKeyRequirement.DISCOURAGED : ResidentKeyRequirement.REQUIRED)
                                        .build())
                        .build());

        byte[] registrationId = new byte[16];
        this.random.nextBytes(registrationId);
        RegistrationStartResponse startResponse = new RegistrationStartResponse(mode,
                Base64.getEncoder().encodeToString(registrationId), credentialCreation);

        registrationOperation.put(startResponse.getRegistrationId(), startResponse);

        return startResponse;
    }

    private String getDisplayName(WebAuthnUser user) {
        String s = (StringUtils.hasLength(user.getFirstName()) ? user.getFirstName() : "") + " " +
                (StringUtils.hasLength(user.getLastName()) ? user.getLastName() : "");
        return StringUtils.hasLength(s.trim()) ? s : user.getUsername();
    }
}
