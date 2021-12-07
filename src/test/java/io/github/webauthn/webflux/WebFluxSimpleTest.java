package io.github.webauthn.webflux;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public class WebFluxSimpleTest {

    @Test
    public void test() {

        Mono.just(1)
                .log()
                .then()
                .switchIfEmpty(Mono.just(2).log().then())
                .block();

    }
}
