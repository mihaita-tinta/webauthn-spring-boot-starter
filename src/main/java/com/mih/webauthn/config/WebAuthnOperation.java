package com.mih.webauthn.config;

public interface WebAuthnOperation<T> {

    void put(String id, T startResponse);

    T get(String id);

    void remove(String id);
}
