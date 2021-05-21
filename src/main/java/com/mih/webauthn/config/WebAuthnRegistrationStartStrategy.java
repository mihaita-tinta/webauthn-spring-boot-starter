package com.mih.webauthn.config;

import com.mih.webauthn.BytesUtil;
import com.mih.webauthn.domain.AppUser;
import com.mih.webauthn.repository.AppCredentialsRepository;
import com.mih.webauthn.repository.AppUserRepository;
import com.mih.webauthn.dto.RegistrationStartRequest;
import com.mih.webauthn.dto.RegistrationStartResponse;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.UserIdentity;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

public class WebAuthnRegistrationStartStrategy {

    private final AppUserRepository appUserRepository;
    private final AppCredentialsRepository credentialRepository;
    private final SecureRandom random = new SecureRandom();
    private final RelyingParty relyingParty;
    private final WebAuthnOperation registrationOperation;

    public WebAuthnRegistrationStartStrategy(AppUserRepository appUserRepository, AppCredentialsRepository credentialRepository, RelyingParty relyingParty, WebAuthnOperation registrationOperation) {
        this.appUserRepository = appUserRepository;
        this.credentialRepository = credentialRepository;
        this.relyingParty = relyingParty;
        this.registrationOperation = registrationOperation;
    }

    public RegistrationStartResponse registrationStart(RegistrationStartRequest request) {

        long userId = -1;
        String name = null;
        RegistrationStartResponse.Mode mode = null;

        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            // cancel if the user is already registered
            boolean isPresent = this.appUserRepository.findByUsername(request.getUsername())
                    .isPresent();
            if (isPresent) {
                throw new IllegalStateException("Username taken");
            }

            AppUser user = new AppUser();
            user.setUsername(request.getUsername());
            userId = this.appUserRepository.save(user)
                    .getId();
            name = request.getUsername();
            mode = RegistrationStartResponse.Mode.NEW;
        } else if (request.getRegistrationAddToken() != null && !request.getRegistrationAddToken().isEmpty()) {
            byte[] registrationAddTokenDecoded = null;
            try {
                registrationAddTokenDecoded = Base64.getDecoder().decode(request.getRegistrationAddToken());
            } catch (Exception e) {
                throw new IllegalStateException("Token invalid");
            }

            AppUser user = appUserRepository.findByAddTokenAndRegistrationAddStartAfter(
                    registrationAddTokenDecoded, LocalDateTime.now().minusMinutes(10))
                    .orElseThrow(() -> new IllegalStateException("Invalid token"));


            userId = user.getId();
            name = user.getUsername();
            mode = RegistrationStartResponse.Mode.ADD;
        } else if (request.getRecoveryToken() != null && !request.getRecoveryToken().isEmpty()) {
            byte[] recoveryTokenDecoded = null;
            try {
                recoveryTokenDecoded = Base64.getDecoder().decode(request.getRecoveryToken());
            } catch (Exception e) {
                throw new IllegalStateException("Token invalid", e);
            }
            AppUser user = appUserRepository.findByRecoveryToken(recoveryTokenDecoded)
                    .orElseThrow(() -> new IllegalStateException("Invalid token"));

            userId = user.getId();
            name = user.getUsername();
            mode = RegistrationStartResponse.Mode.RECOVERY;
            credentialRepository.deleteByAppUserId(userId);

        }

        if (mode != null) {
            PublicKeyCredentialCreationOptions credentialCreation = this.relyingParty
                    .startRegistration(StartRegistrationOptions.builder()
                            .user(UserIdentity.builder().name(name).displayName(name)
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
