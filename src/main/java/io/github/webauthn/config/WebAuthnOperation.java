package io.github.webauthn.config;

import java.util.stream.Stream;

public interface WebAuthnOperation<T, K> {

    void put(K id, T data);

    T get(K id);

    void remove(K id);

    Stream<T> list();
}
