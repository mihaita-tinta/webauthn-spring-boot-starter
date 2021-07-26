package com.mih.webauthn.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mih.webauthn.WebAuthnFilter;
import com.mih.webauthn.WebAuthnProperties;
import com.mih.webauthn.domain.*;
import com.mih.webauthn.dto.AssertionStartResponse;
import com.mih.webauthn.dto.RegistrationStartResponse;
import com.mih.webauthn.service.DefaultCredentialService;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WebauthnConfigurer extends AbstractHttpConfigurer<WebauthnConfigurer, HttpSecurity> {

    private Consumer<WebAuthnUser> loginSuccessHandler = (user) -> {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(token);
    };
    private Consumer<WebAuthnUser> registerSuccessHandler;

    private Supplier<WebAuthnUser> userSupplier = () -> {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return (WebAuthnUser) token.getPrincipal();
    };

    private WebAuthnUserRepository userRepository;
    private WebAuthnCredentialsRepository credentialsRepository;
    private WebAuthnOperation<AssertionStartResponse, String> assertionResponseCache;
    private WebAuthnOperation<RegistrationStartResponse, String> registrationStartResponseCache;
    private RelyingParty relyingParty;

    private WebAuthnFilter filter;

    public WebauthnConfigurer() {
    }

    @Override
    public void init(HttpSecurity http) {
    }

    public WebauthnConfigurer successHandler(Consumer<WebAuthnUser> successHandler) {
        Assert.notNull(successHandler, "successHandler cannot be null");
        this.loginSuccessHandler = successHandler;
        return this;
    }

    public WebauthnConfigurer defaultLoginSuccessHandler(Consumer<WebAuthnUser> andThen) {
        Assert.notNull(andThen, "andThen cannot be null");
        this.loginSuccessHandler = loginSuccessHandler.andThen(andThen);
        return this;
    }

    public WebauthnConfigurer registerSuccessHandler(Consumer<WebAuthnUser> registerSuccessHandler) {
        Assert.notNull(registerSuccessHandler, "registerSuccessHandler cannot be null");
        this.registerSuccessHandler = registerSuccessHandler;
        return this;
    }

    public WebauthnConfigurer userSupplier(Supplier<WebAuthnUser> userSupplier) {
        Assert.notNull(userSupplier, "userSupplier cannot be null");
        this.userSupplier = userSupplier;
        return this;
    }
    public WebauthnConfigurer userRepository(WebAuthnUserRepository userRepository) {
        Assert.notNull(userRepository, "userRepository cannot be null");
        this.userRepository = userRepository;
        return this;
    }
    public WebauthnConfigurer credentialsRepository(WebAuthnCredentialsRepository credentialsRepository) {
        Assert.notNull(credentialsRepository, "credentialsRepository cannot be null");
        this.credentialsRepository = credentialsRepository;
        return this;
    }
    public WebauthnConfigurer relyingParty(RelyingParty relyingParty) {
        Assert.notNull(relyingParty, "relyingParty cannot be null");
        this.relyingParty = relyingParty;
        return this;
    }

    @Override
    public void configure(HttpSecurity http) {

        this.filter = new WebAuthnFilter();

        this.filter.registerDefaults(
                getOrCreateUserRepository(),
                getOrCreateCredentialsRepository(),
                getOrCreateRelyingParty(http),
                getBean(http, ObjectMapper.class),
                getOrCreateRegistrationStartResponseCache(),
                getOrCreateWebAuthnAssertionCache()
                );

        this.filter.setSuccessHandler(loginSuccessHandler);
        this.filter.setUserSupplier(userSupplier);
        this.filter.setRegisterSuccessHandler(registerSuccessHandler);

        http.addFilterBefore(filter, BasicAuthenticationFilter.class);
    }

    private <T> T getBean(HttpSecurity http, Class<T> clasz) {
        return http.getSharedObject(ApplicationContext.class).getBean(clasz);
    }

    private WebAuthnUserRepository getOrCreateUserRepository() {
        return this.userRepository != null ? userRepository : new WebAuthnUserInMemoryRepository();
    }
    private WebAuthnCredentialsRepository getOrCreateCredentialsRepository() {
        return this.credentialsRepository != null ? credentialsRepository : new WebAuthnCredentialsInMemoryRepository();
    }

    private WebAuthnOperation<AssertionStartResponse, String> getOrCreateWebAuthnAssertionCache() {
        return this.assertionResponseCache != null ? assertionResponseCache : new InMemoryOperation();
    }
    private WebAuthnOperation<RegistrationStartResponse, String> getOrCreateRegistrationStartResponseCache() {
        return this.registrationStartResponseCache != null ? registrationStartResponseCache : new InMemoryOperation();
    }

    private CredentialRepository credentialRepositoryService(WebAuthnCredentialsRepository credentialsRepository,
                                                            WebAuthnUserRepository userRepository) {
        return new DefaultCredentialService(credentialsRepository, userRepository);
    }

    private RelyingParty getOrCreateRelyingParty(HttpSecurity http) {
        WebAuthnProperties props = getBean(http, WebAuthnProperties.class);
        return this.relyingParty != null ? relyingParty : relyingParty(new DefaultCredentialService(credentialsRepository, userRepository), props);
    }

    private RelyingParty relyingParty(CredentialRepository credentialRepository, WebAuthnProperties appProperties) {

        RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
                .id(appProperties.getRelyingPartyId()).name(appProperties.getRelyingPartyName())
                .icon(Optional.ofNullable(appProperties.getRelyingPartyIcon())).build();

        return RelyingParty.builder().identity(rpIdentity)
                .credentialRepository(credentialRepository)
                .origins(appProperties.getRelyingPartyOrigins()).build();
    }


}
