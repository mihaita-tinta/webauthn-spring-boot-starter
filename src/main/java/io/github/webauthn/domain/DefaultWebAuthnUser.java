package io.github.webauthn.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;

public class DefaultWebAuthnUser implements UserDetails, WebAuthnUser {

    private Long id;
    private String username;
    private boolean enabled;
    private byte[] recoveryToken;
    private byte[] addToken;
    private LocalDateTime registrationAddStart;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public byte[] getRecoveryToken() {
        return recoveryToken;
    }

    @Override
    public void setRecoveryToken(byte[] recoveryToken) {
        this.recoveryToken = recoveryToken;
    }

    @Override
    public byte[] getAddToken() {
        return addToken;
    }

    @Override
    public void setAddToken(byte[] addToken) {
        this.addToken = addToken;
    }

    @Override
    public LocalDateTime getRegistrationAddStart() {
        return registrationAddStart;
    }

    @Override
    public void setRegistrationAddStart(LocalDateTime registrationAddStart) {
        this.registrationAddStart = registrationAddStart;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultWebAuthnUser that = (DefaultWebAuthnUser) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "WebAuthnUser{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }

}
