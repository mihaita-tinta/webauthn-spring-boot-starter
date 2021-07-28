package com.mih.webauthn.domain;

public interface WebAuthnCredentials {

    byte[] getCredentialId();
    Long getAppUserId();
    Long getCount();
    void setCount(Long count);
    byte[] getPublicKeyCose();
    String getUserAgent();
}
