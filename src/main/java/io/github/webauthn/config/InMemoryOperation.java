package io.github.webauthn.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class InMemoryOperation<T, K> implements WebAuthnOperation<T, K> {

    private final Map<K, T> cache = new ConcurrentHashMap<>();

    @Override
    public void put(K id, T data) {
        cache.put(id, data);
    }

    @Override
    public T get(K id) {
        return cache.get(id);
    }

    @Override
    public void remove(K id) {
        cache.remove(id);
    }

    @Override
    public Stream<T> list() {
        return cache.entrySet().stream()
                .map(entry -> entry.getValue());
    }
}
