package io.github.webauthn.webflux;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.webauthn.EnableWebAuthn;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.config.EnableWebFlux;
import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableWebFlux
@EnableWebFluxSecurity
@EnableWebAuthn
public class SpringWebFluxTestConfig {
    private static final Logger log = LoggerFactory.getLogger(SpringWebFluxTestConfig.class);
    @Autowired
    WebAuthnWebFilter webAuthnWebFilter;
    @Autowired
    WebAuthnUserRepository userRepository;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

        http
                .authorizeExchange()
                .anyExchange()
                .authenticated()
                .and()
                .cors()
                .and()
                .addFilterAfter(webAuthnWebFilter.with(ReactiveSecurityContextHolder.getContext()
                        .flatMap(sc -> {
                            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) sc.getAuthentication();
                            if (token == null)
                                return Mono.empty();

                            Object principal = token.getPrincipal();
                            if (principal instanceof WebAuthnUser) {
                                return Mono.just((WebAuthnUser) principal);
                            } else {
                                WebAuthnUser u = new WebAuthnUser();
                                u.setUsername(token.getName());

                                return Mono.just(userRepository.findByUsername(u.getUsername()).orElseGet(() ->
                                        userRepository.save(u)
                                ));
                            }
                        })), SecurityWebFiltersOrder.AUTHENTICATION)
                .csrf()
                .disable()
        ;

        return http.build();
    }
//
//    @GetMapping("/api/test")
//    public String get(@AuthenticationPrincipal Authentication authentication) {
//        return authentication.getName();
//    }
}
