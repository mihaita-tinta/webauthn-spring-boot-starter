package com.mih.webauthn.domain;

public class WebAuthnDefaultCredentials implements WebAuthnCredentials {
    private byte[] credentialId;
    private Long appUserId;
    private Long count;
    private byte[] publicKeyCose;

    public WebAuthnDefaultCredentials(byte[] credentialId, Long appUserId, Long count, byte[] publicKeyCose) {
        this.credentialId = credentialId;
        this.appUserId = appUserId;
        this.count = count;
        this.publicKeyCose = publicKeyCose;
    }

    public WebAuthnDefaultCredentials() {

    }

    public byte[] getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(byte[] credentialId) {
        this.credentialId = credentialId;
    }

    public Long getAppUserId() {
        return appUserId;
    }

    public void setAppUserId(Long appUserId) {
        this.appUserId = appUserId;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public byte[] getPublicKeyCose() {
        return publicKeyCose;
    }

    public void setPublicKeyCose(byte[] publicKeyCose) {
        this.publicKeyCose = publicKeyCose;
    }
}
