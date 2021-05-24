package com.mih.webauthn.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryOperation<T> implements WebAuthnOperation<T> {

    private final Map<String, T> cache = new ConcurrentHashMap<>();

    @Override
    public void put(String id, T data) {
        cache.put(id, data);
    }

    @Override
    public T get(String id) {
        return cache.get(id);
    }

    @Override
    public void remove(String id) {
        cache.remove(id);
    }
}
