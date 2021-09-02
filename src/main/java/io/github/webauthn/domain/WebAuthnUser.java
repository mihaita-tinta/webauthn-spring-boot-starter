package io.github.webauthn.domain;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(uniqueConstraints =
@UniqueConstraint(columnNames = "username"))
public class WebAuthnUser {

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

    public String getUsername() {
        return username;
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
