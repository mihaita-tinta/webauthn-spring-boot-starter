package io.github.webauthn.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(uniqueConstraints =
@UniqueConstraint(columnNames = "username"))
public class WebAuthnUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String username;
    private byte[] recoveryToken;
    private byte[] addToken;
    private LocalDateTime registrationAddStart;

    public Long getId() {
        return id;
    }

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
        return true;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte[] getRecoveryToken() {
        return recoveryToken;
    }

    public void setRecoveryToken(byte[] recoveryToken) {
        this.recoveryToken = recoveryToken;
    }

    public byte[] getAddToken() {
        return addToken;
    }

    public void setAddToken(byte[] addToken) {
        this.addToken = addToken;
    }

    public LocalDateTime getRegistrationAddStart() {
        return registrationAddStart;
    }

    public void setRegistrationAddStart(LocalDateTime registrationAddStart) {
        this.registrationAddStart = registrationAddStart;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebAuthnUser that = (WebAuthnUser) o;
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
