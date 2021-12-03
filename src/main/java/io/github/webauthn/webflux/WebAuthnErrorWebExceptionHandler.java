package io.github.webauthn.webflux;

import io.github.webauthn.flows.InvalidTokenException;
import io.github.webauthn.flows.WebAuthnAssertionFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class WebAuthnErrorWebExceptionHandler implements ErrorWebExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(WebAuthnErrorWebExceptionHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange serverWebExchange, Throwable throwable) {
        log.error("handle - error: ", throwable);
        if (throwable instanceof UsernameNotFoundException ||
                throwable instanceof WebAuthnAssertionFailedException) {
            serverWebExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return serverWebExchange.getResponse().writeWith(Mono.empty());
        } else if (throwable instanceof InvalidTokenException) {
            serverWebExchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            return serverWebExchange.getResponse().writeWith(Mono.empty());
        }
        serverWebExchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return serverWebExchange.getResponse().setComplete();
    }
}
