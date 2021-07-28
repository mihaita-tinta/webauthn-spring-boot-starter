package com.mih.webauthn.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mih.webauthn.WebAuthnFilter;
import com.mih.webauthn.WebAuthnProperties;
import com.mih.webauthn.domain.WebAuthnCredentials;
import com.mih.webauthn.domain.WebAuthnCredentialsRepository;
import com.mih.webauthn.domain.WebAuthnUser;
import com.mih.webauthn.domain.WebAuthnUserRepository;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WebauthnConfigurer extends AbstractHttpConfigurer<WebauthnConfigurer, HttpSecurity> {

    private BiConsumer<WebAuthnUser, WebAuthnCredentials> loginSuccessHandler = (user, credentials) -> {
        UsernamePasswordAuthenticationToken token = new WebAuthnUsernameAuthenticationToken(user, credentials, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(token);
    };
    private Consumer<WebAuthnUser> registerSuccessHandler;

    private Supplier<WebAuthnUser> userSupplier = () -> {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return (WebAuthnUser) token.getPrincipal();
    };

    private WebAuthnFilter filter;

    public WebauthnConfigurer() {
    }

    @Override
    public void init(HttpSecurity http) {
    }

    public WebauthnConfigurer successHandler(BiConsumer<WebAuthnUser, WebAuthnCredentials> successHandler) {
        Assert.notNull(successHandler, "successHandler cannot be null");
        this.loginSuccessHandler = successHandler;
        return this;
    }

    public WebauthnConfigurer defaultLoginSuccessHandler(BiConsumer<WebAuthnUser, WebAuthnCredentials> andThen) {
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

    @Override
    public void configure(HttpSecurity http) {

        this.filter = new WebAuthnFilter();

        this.filter.registerDefaults(
                getBean(http, WebAuthnUserRepository.class),
                getBean(http, WebAuthnCredentialsRepository.class),
                getBean(http, RelyingParty.class),
                getBean(http, ObjectMapper.class),
                getBean(http,  WebAuthnOperation.class),
                getBean(http,  WebAuthnOperation.class)
                );

        this.filter.setSuccessHandler(loginSuccessHandler);
        this.filter.setUserSupplier(userSupplier);
        this.filter.setRegisterSuccessHandler(registerSuccessHandler);

        http.addFilterBefore(filter, BasicAuthenticationFilter.class);
    }

    private <T> T getBean(HttpSecurity http, Class<T> clasz) {
        return http.getSharedObject(ApplicationContext.class).getBean(clasz);
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
