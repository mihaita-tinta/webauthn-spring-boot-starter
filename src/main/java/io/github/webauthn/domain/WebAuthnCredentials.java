package io.github.webauthn.domain;

public interface WebAuthnCredentials {
    byte[] getCredentialId();

    void setCredentialId(byte[] credentialId);

    Long getAppUserId();

    void setAppUserId(Long appUserId);

    Long getCount();

    void setCount(Long count);

    byte[] getPublicKeyCose();

    void setPublicKeyCose(byte[] publicKeyCose);

    String getUserAgent();

    void setUserAgent(String userAgent);

    Long getId();

    void setId(Long id);
}
