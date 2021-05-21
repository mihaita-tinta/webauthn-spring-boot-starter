package com.mih.webauthn.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryOperation<T> implements WebAuthnOperation<T> {

    private final Map<String, T> cache = new ConcurrentHashMap<>();

    @Override
    public void put(String registrationId, T startResponse) {
        cache.put(registrationId, startResponse);
    }

    @Override
    public T get(String registrationId) {
        return cache.get(registrationId);
    }

    @Override
    public void remove(String registrationId) {
        cache.remove(registrationId);
    }
}
