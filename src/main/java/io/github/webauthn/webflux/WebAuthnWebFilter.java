package io.github.webauthn.webflux;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.webauthn.RelyingParty;
import io.github.webauthn.WebAuthnProperties;
import io.github.webauthn.config.WebAuthnOperation;
import io.github.webauthn.config.WebAuthnUsernameAuthenticationToken;
import io.github.webauthn.domain.DefaultWebAuthnUser;
import io.github.webauthn.domain.WebAuthnCredentials;
import io.github.webauthn.domain.WebAuthnCredentialsRepository;
import io.github.webauthn.domain.WebAuthnUser;
import io.github.webauthn.domain.WebAuthnUserRepository;
import io.github.webauthn.dto.AssertionFinishRequest;
import io.github.webauthn.dto.AssertionStartRequest;
import io.github.webauthn.dto.AssertionStartResponse;
import io.github.webauthn.dto.RegistrationFinishRequest;
import io.github.webauthn.dto.RegistrationStartRequest;
import io.github.webauthn.dto.RegistrationStartResponse;
import io.github.webauthn.events.WebAuthnEventPublisher;
import io.github.webauthn.flows.WebAuthnAssertionFinishStrategy;
import io.github.webauthn.flows.WebAuthnAssertionStartStrategy;
import io.github.webauthn.flows.WebAuthnRegistrationAddStrategy;
import io.github.webauthn.flows.WebAuthnRegistrationFinishStrategy;
import io.github.webauthn.flows.WebAuthnRegistrationStartStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureDisabledEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.springframework.core.ResolvableType.forClass;

public class WebAuthnWebFilter implements WebFilter {
    private static final Logger log = LoggerFactory.getLogger(WebAuthnWebFilter.class);
    private final String FILTER_NAME_APPLIED = this.getClass().getName();
    private final ServerWebExchangeMatcher registrationStartPath;
    private final ServerWebExchangeMatcher registrationFinishPath;
    private final ServerWebExchangeMatcher registrationAddPath;
    private final ServerWebExchangeMatcher assertionStartPath;
    private final ServerWebExchangeMatcher assertionFinishPath;
    private final WebAuthnRegistrationStartStrategy startStrategy;
    private final WebAuthnRegistrationAddStrategy addStrategy;
    private final WebAuthnRegistrationFinishStrategy finishStrategy;
    private final WebAuthnAssertionStartStrategy assertionStartStrategy;
    private final WebAuthnAssertionFinishStrategy assertionFinishStrategy;
    private final ServerSecurityContextRepository serverSecurityContextRepository;
    private final WebAuthnEventPublisher publisher;

    private BiFunction<WebAuthnUser, WebAuthnCredentials, Mono<Authentication>> successHandler = (user, credentials) ->
            Mono.just(new WebAuthnUsernameAuthenticationToken(user, credentials, Collections.emptyList()));
    private BiFunction<WebAuthnAssertionFinishStrategy.AssertionSuccessResponse, Authentication, Object> authenticationSuccessResponseMapper = (finish, authentication) ->
            Map.of("username", authentication.getName());

    private Mono<? extends WebAuthnUser> userSupplier = ReactiveSecurityContextHolder.getContext()
            .flatMap(sc -> {
                UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) sc.getAuthentication();
                if (token == null)
                    return Mono.empty();

                Object principal = token.getPrincipal();
                if (principal instanceof DefaultWebAuthnUser) {
                    return Mono.just((DefaultWebAuthnUser) principal);
                } else {
                    log.warn("userSupplier - you need to configure your WebAuthnWebFilter.userSupplier method to tranform your principal implementation to something that webauthn starter can understand");
                }
                return Mono.empty();
            });

    private final Jackson2JsonEncoder encoder;
    private final Jackson2JsonDecoder decoder = new Jackson2JsonDecoder();
    private final Executor executor = Executors.newFixedThreadPool(100);

    public WebAuthnWebFilter(WebAuthnProperties properties, WebAuthnUserRepository appUserRepository,
                             WebAuthnCredentialsRepository credentialRepository, RelyingParty relyingParty, ObjectMapper mapper,
                             WebAuthnOperation<RegistrationStartResponse, String> registrationOperation,
                             WebAuthnOperation<AssertionStartResponse, String> assertionOperation, ServerSecurityContextRepository serverSecurityContextRepository,
                             WebAuthnEventPublisher publisher) {
        this.registrationStartPath = properties.getEndpoints().getRegistrationStartPathWebFlux();
        this.registrationAddPath = properties.getEndpoints().getRegistrationAddPathWebFlux();
        this.registrationFinishPath = properties.getEndpoints().getRegistrationFinishPathWebFlux();
        this.assertionStartPath = properties.getEndpoints().getAssertionStartPathWebFlux();
        this.assertionFinishPath = properties.getEndpoints().getAssertionFinishPathWebFlux();
        this.encoder = new Jackson2JsonEncoder(mapper);
        this.serverSecurityContextRepository = serverSecurityContextRepository;
        this.publisher = publisher;

        this.startStrategy = new WebAuthnRegistrationStartStrategy(appUserRepository,
                credentialRepository, relyingParty, registrationOperation, properties);
        this.addStrategy = new WebAuthnRegistrationAddStrategy(appUserRepository, publisher);
        this.finishStrategy = new WebAuthnRegistrationFinishStrategy(appUserRepository,
                credentialRepository, relyingParty, registrationOperation, publisher);

        this.assertionStartStrategy = new WebAuthnAssertionStartStrategy(relyingParty, assertionOperation);
        this.assertionFinishStrategy = new WebAuthnAssertionFinishStrategy(appUserRepository,
                credentialRepository, relyingParty, assertionOperation);
    }

    public WebAuthnWebFilter withUser(Mono<? extends WebAuthnUser> userSupplier) {
        this.userSupplier = userSupplier;
        return this;
    }

    public WebAuthnWebFilter withLoginSuccessHandler(BiFunction<WebAuthnUser, WebAuthnCredentials, Mono<Authentication>> successHandler) {
        this.successHandler = successHandler;
        return this;
    }

    public WebAuthnWebFilter withAuthenticationSuccessResponseMapper(BiFunction<WebAuthnAssertionFinishStrategy.AssertionSuccessResponse, Authentication, Object> authenticationSuccessHandler) {
        this.authenticationSuccessResponseMapper = authenticationSuccessHandler;
        return this;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange,
                             WebFilterChain webFilterChain) {

        log.debug("filter - path: {}", serverWebExchange.getRequest().getPath());
        return route(assertionStartPath, this::handleAssertionStart, serverWebExchange)
                .switchIfEmpty(route(assertionFinishPath, this::handleAssertionFinish, serverWebExchange))
                .switchIfEmpty(route(registrationStartPath, this::handleRegistrationStart, serverWebExchange))
                .switchIfEmpty(route(registrationFinishPath, this::handleRegistrationFinish, serverWebExchange))
                .switchIfEmpty(route(registrationAddPath, this::handleRegistrationAdd, serverWebExchange))
                .switchIfEmpty(webFilterChain.filter(serverWebExchange))
                .flatMap(res -> writeResponseBody(res, serverWebExchange))
                .subscribeOn(Schedulers.boundedElastic());

    }

    private Mono<Object> route(ServerWebExchangeMatcher matcher, Function<ServerWebExchange, Mono<Object>> handler,
                               ServerWebExchange serverWebExchange) {
        return matcher.matches(serverWebExchange)
                .flatMap(matchResult -> {
                    if (!matchResult.isMatch()) {
                        return Mono.empty();
                    }
                    return handler.apply(serverWebExchange);
                });
    }

    private Mono<Object> handleAssertionFinish(ServerWebExchange serverWebExchange) {
        return decode(serverWebExchange, AssertionFinishRequest.class)
                .flatMap(t -> Mono.fromFuture(CompletableFuture.supplyAsync(() ->
                        assertionFinishStrategy.finish(t), executor))
                )
                .flatMap(finish -> {
                    if (finish.isPresent()) {
                        log.debug("handleAssertionFinish - success {}" + finish.get());
                        Mono<Authentication> auth = successHandler.apply(finish.get().getUser(), finish.get().credentials());
                        return auth.map(a -> new SecurityContextImpl(a))
                                .flatMap(securityContext -> this.serverSecurityContextRepository.save(serverWebExchange, securityContext)
                                        .then(Mono.just(authenticationSuccessResponseMapper.apply(finish.get(), securityContext.getAuthentication())))
                                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                                        .map(c -> {
                                            publisher.publishEvent(new AuthenticationSuccessEvent(securityContext.getAuthentication()));
                                            return c;
                                        }));
                    }
                    return Mono.error(new BadCredentialsException("Assertion finish failed"));
                });
    }

    private Mono<Object> handleRegistrationStart(ServerWebExchange serverWebExchange) {
        return decode(serverWebExchange, RegistrationStartRequest.class)
                .zipWith(userSupplier.map(Optional::of).defaultIfEmpty(Optional.empty()))
                .flatMap(t -> Mono.fromFuture(CompletableFuture.supplyAsync(() ->
                        startStrategy.registrationStart(t.getT1(), t.getT2()), executor))
                );
    }

    private Mono<Object> handleRegistrationFinish(ServerWebExchange serverWebExchange) {
        return decode(serverWebExchange, RegistrationFinishRequest.class)
                .flatMap(req -> Mono.fromFuture(CompletableFuture.supplyAsync(() ->
                        finishStrategy.registrationFinish(req), executor))
                );
    }

    private Mono<Object> handleRegistrationAdd(ServerWebExchange serverWebExchange) {
        return userSupplier
                .map(user -> (Object) addStrategy.registrationAdd(user))
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("no user found")));
    }

    private Mono<Object> handleAssertionStart(ServerWebExchange serverWebExchange) {
        return decode(serverWebExchange, AssertionStartRequest.class)
                .flatMap(req -> Mono.fromFuture(CompletableFuture.supplyAsync(() ->
                        assertionStartStrategy.start(req), executor))
                );
    }

    <T> Mono<T> decode(ServerWebExchange serverWebExchange, Class<T> clasz) {
        ResolvableType elementType = ResolvableType.forClass(clasz);
        return decoder.decodeToMono(serverWebExchange.getRequest().getBody(), elementType, MediaType.APPLICATION_JSON, Collections.emptyMap()).cast(clasz);
    }

    Mono<Void> writeResponseBody(Object res, ServerWebExchange serverWebExchange) {
        log.debug("write - response: {}", res);
        DataBuffer dataBuffer = encoder.encodeValue(res, serverWebExchange.getResponse().bufferFactory(), forClass(res.getClass()), MediaType.APPLICATION_JSON, Collections.emptyMap());
        serverWebExchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        serverWebExchange.getResponse().setStatusCode(HttpStatus.OK);
        return serverWebExchange.getResponse().writeWith(Mono.just(dataBuffer))
                .doOnError((error) -> DataBufferUtils.release(dataBuffer));
    }
}
