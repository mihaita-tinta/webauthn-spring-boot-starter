package io.github.webauthn.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Arrays;
import java.util.Objects;

@Entity
public class WebAuthnCredentials {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private byte[] credentialId;
    private Long appUserId;
    private Long count;
    private byte[] publicKeyCose;
    private String userAgent;

    public WebAuthnCredentials(byte[] credentialId, Long appUserId, Long count, byte[] publicKeyCose, String userAgent) {
        this.credentialId = credentialId;
        this.appUserId = appUserId;
        this.count = count;
        this.publicKeyCose = publicKeyCose;
        this.userAgent = userAgent;
    }

    public WebAuthnCredentials() {

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

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebAuthnCredentials that = (WebAuthnCredentials) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "WebAuthnCredentials{" +
                "id=" + id +
                ", credentialId=" + Arrays.toString(credentialId) +
                ", appUserId=" + appUserId +
                '}';
    }
}
