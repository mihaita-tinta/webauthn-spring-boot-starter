package io.github.webauthn.webflux;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.webauthn.RelyingParty;
import io.github.webauthn.WebAuthnProperties;
import io.github.webauthn.config.WebAuthnOperation;
import io.github.webauthn.domain.WebAuthnCredentialsRepository;
import io.github.webauthn.domain.WebAuthnUserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.server.WebHandler;
import org.springframework.web.servlet.DispatcherServlet;

@Configuration
@ConditionalOnClass(WebHandler.class)
@ConditionalOnMissingBean(DispatcherServlet.class)
public class WebAuthnWebFluxConfig {

    @Bean
    @ConditionalOnMissingBean
    public WebSessionServerSecurityContextRepository serverSecurityContextRepository() {
        return new WebSessionServerSecurityContextRepository();
    }
    @Bean
    public WebAuthnErrorWebExceptionHandler webAuthnErrorWebExceptionHandler() {
        return new WebAuthnErrorWebExceptionHandler();
    }

    @Bean
    public WebAuthnWebFilter webAuthnWebFilter(WebAuthnProperties properties,
                                               WebAuthnUserRepository webAuthnUserRepository,
                                               WebAuthnCredentialsRepository credentialsRepository,
                                               RelyingParty rp,
                                               ObjectMapper mapper,
                                               WebAuthnOperation registration,
                                               WebAuthnOperation assertion,
                                               ServerSecurityContextRepository serverSecurityContextRepository) {
        return new WebAuthnWebFilter(properties,
                webAuthnUserRepository,
                credentialsRepository,
                rp,
                mapper, registration, assertion, serverSecurityContextRepository);
    }

//    @Bean
//    RouterFunction<ServerResponse> routes(WebAuthnWebFluxProperties properties, WebAuthnAssertionStartWebFilter handler) {
//        return route(GET(properties.getEndpoints().getAssertionStartPath()), handler::handle)
//                .andRoute(GET("/profiles/{id}"), handler::handle);
////                .andRoute(DELETE("/profiles/{id}"), handler::deleteById)
////                .andRoute(POST("/profiles"), handler::create)
////                .andRoute(PUT("/profiles/{id}"), handler::updateById);
//    }

//    @Bean
//    public AuthenticationWebFilter bearerAuthenticationFilter() {
//        ReactiveAuthenticationManager authManager = new BearerTokenReactiveAuthenticationManager();
//        Function<ServerWebExchange, Mono<Authentication>> bearerConverter = new ServerHttpBearerAuthenticationConverter(tokenProvider);
//
//        AuthenticationWebFilter bearerAuthenticationFilter = new AuthenticationWebFilter(authManager);
//        bearerAuthenticationFilter.setAuthenticationConverter(bearerConverter);
//        bearerAuthenticationFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/api/**", "/actuator/**"));
//
//        return bearerAuthenticationFilter;
//    }
}
