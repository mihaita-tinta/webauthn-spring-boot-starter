package com.mih.webauthn.domain;

import org.springframework.data.repository.CrudRepository;

public interface MyCredentialsRepository extends CrudRepository<MyCredentials, Long>, WebAuthnCredentialsRepository {
}
