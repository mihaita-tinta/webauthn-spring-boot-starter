package com.mih.webauthn.service;

import com.mih.webauthn.BytesUtil;
import com.mih.webauthn.domain.AppCredentials;
import com.mih.webauthn.repository.AppCredentialsRepository;
import com.mih.webauthn.repository.AppUserRepository;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


public class CredentialService implements CredentialRepository {

    private final AppCredentialsRepository credentialsRepository;
    private final AppUserRepository appUserRepository;

    public CredentialService(AppCredentialsRepository credentialsRepository, AppUserRepository appUserRepository) {
        this.credentialsRepository = credentialsRepository;
        this.appUserRepository = appUserRepository;
    }

    public AppCredentials addCredential(long userId, byte[] credentialId, byte[] publicKeyCose,
                                        long counter) {

        return credentialsRepository.save(new AppCredentials(credentialId, userId, counter, publicKeyCose));
    }

    public AppCredentials updateSignatureCount(AssertionResult result) {
        System.out.println("updateSignatureCount: " + result.getUserHandle());

        long appUserId = BytesUtil.bytesToLong(result.getUserHandle().getBytes());
        byte[] credentialId = result.getCredentialId().getBytes();

        AppCredentials appCredentials = credentialsRepository.findByCredentialIdAndAppUserId(credentialId, appUserId)
                .map(credential -> {
                    credential.setCount(result.getSignatureCount());
                    return credential;
                })
                .orElseThrow();
        return credentialsRepository.save(appCredentials);
    }

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {

        return appUserRepository.findByUsername(username)
                .map(user -> credentialsRepository.findAllByAppUserId(user.getId())
                        .stream()
                        .map(credential -> PublicKeyCredentialDescriptor.builder()
                                .id(new ByteArray(credential.getCredentialId())).build())
                        .collect(Collectors.toSet())
                ).orElseThrow();
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        return appUserRepository.findByUsername(username)
                .map(user -> Optional.of(new ByteArray(BytesUtil.longToBytes(user.getId()))))
                .orElse(Optional.empty());
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray byteArray) {
        return Optional.empty();
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId,
                                                 ByteArray userHandle) {
        System.out.println("JCR: lookup: " + credentialId + ":"
                + BytesUtil.bytesToLong(userHandle.getBytes()));

        long id = BytesUtil.bytesToLong(userHandle.getBytes());

        return appUserRepository.findById(id)
                .map(user -> credentialsRepository.findByCredentialIdAndAppUserId(credentialId.getBytes(), id)
                        .map(credential -> RegisteredCredential.builder()
                                .credentialId(new ByteArray(credential.getCredentialId()))
                                .userHandle(userHandle)
                                .publicKeyCose(new ByteArray(credential.getPublicKeyCose()))
                                .signatureCount(credential.getCount()).build()))
                        .orElse(Optional.empty());
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {

        return credentialsRepository.findByCredentialId(credentialId.getBytes())
                .stream()
                .map(credential -> RegisteredCredential.builder()
                        .credentialId(new ByteArray(credential.getCredentialId()))
                        .userHandle(new ByteArray(BytesUtil.longToBytes(credential.getAppUserId())))
                        .publicKeyCose(new ByteArray(credential.getPublicKeyCose()))
                        .signatureCount(credential.getCount()).build())
                .collect(Collectors.toSet());
    }
}
