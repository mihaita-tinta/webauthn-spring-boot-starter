package com.mih.webauthn.service;

import com.mih.webauthn.BytesUtil;
import com.mih.webauthn.repository.WebAuthnCredentialsRepository;
import com.mih.webauthn.repository.WebAuthnUserRepository;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


public class DefaultCredentialService implements CredentialRepository {

    private final WebAuthnCredentialsRepository webAuthnCredentialsRepository;
    private final WebAuthnUserRepository webAuthnUserRepository;

    public DefaultCredentialService(WebAuthnCredentialsRepository webAuthnCredentialsRepository, WebAuthnUserRepository webAuthnUserRepository) {
        this.webAuthnCredentialsRepository = webAuthnCredentialsRepository;
        this.webAuthnUserRepository = webAuthnUserRepository;
    }

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {

        return webAuthnUserRepository.findByUsername(username)
                .map(user -> webAuthnCredentialsRepository.findAllByAppUserId(user.getId())
                        .stream()
                        .map(credential -> PublicKeyCredentialDescriptor.builder()
                                .id(new ByteArray(credential.getCredentialId())).build())
                        .collect(Collectors.toSet())
                ).orElseThrow();
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        return webAuthnUserRepository.findByUsername(username)
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

        return webAuthnUserRepository.findById(id)
                .map(user -> webAuthnCredentialsRepository.findByCredentialIdAndAppUserId(credentialId.getBytes(), id)
                        .map(credential -> RegisteredCredential.builder()
                                .credentialId(new ByteArray(credential.getCredentialId()))
                                .userHandle(userHandle)
                                .publicKeyCose(new ByteArray(credential.getPublicKeyCose()))
                                .signatureCount(credential.getCount()).build()))
                .orElse(Optional.empty());
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {

        return webAuthnCredentialsRepository.findByCredentialId(credentialId.getBytes())
                .stream()
                .map(credential -> RegisteredCredential.builder()
                        .credentialId(new ByteArray(credential.getCredentialId()))
                        .userHandle(new ByteArray(BytesUtil.longToBytes(credential.getAppUserId())))
                        .publicKeyCose(new ByteArray(credential.getPublicKeyCose()))
                        .signatureCount(credential.getCount()).build())
                .collect(Collectors.toSet());
    }
}
