package io.github.webauthn.webflux;

import io.github.webauthn.EnableWebAuthn;
import io.github.webauthn.domain.DefaultWebAuthnUser;
import io.github.webauthn.domain.WebAuthnUser;
import io.github.webauthn.domain.WebAuthnUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.config.EnableWebFlux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Supplier;

@SpringBootApplication
@EnableWebFlux
@EnableWebFluxSecurity
@EnableWebAuthn
public class SpringWebFluxTestConfig {
    private static final Logger log = LoggerFactory.getLogger(SpringWebFluxTestConfig.class);
    @Autowired
    Supplier<WebAuthnWebFilter> webAuthnWebFilterSupplier;
    @Autowired
    WebAuthnUserRepository<WebAuthnUser> userRepository;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

        http
                .authorizeExchange()
                .anyExchange()
                .authenticated()
                .and()
                .cors()
                .and()
                .addFilterAfter(webAuthnWebFilterSupplier.get()
                                .withAuthenticationSuccessResponseMapper((finish, authentication) ->
                                        Map.of("name", authentication.getName()))
                                .withUser(ReactiveSecurityContextHolder.getContext()
                                        .flatMap(sc -> {
                                            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) sc.getAuthentication();
                                            if (token == null)
                                                return Mono.empty();

                                            Object principal = token.getPrincipal();
                                            if (principal instanceof DefaultWebAuthnUser) {
                                                return Mono.just((DefaultWebAuthnUser) principal);
                                            } else {
                                                DefaultWebAuthnUser u = new DefaultWebAuthnUser();
                                                u.setUsername(token.getName());

                                                return Mono.just(userRepository.findByUsername(u.getUsername()).orElseGet(() ->
                                                        userRepository.save(u)
                                                ));
                                            }
                                        }))
                        , SecurityWebFiltersOrder.AUTHENTICATION)
                .csrf()
                .disable()
        ;

        return http.build();
    }

}
