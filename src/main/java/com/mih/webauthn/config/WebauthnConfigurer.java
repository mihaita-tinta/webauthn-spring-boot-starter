package com.mih.webauthn.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mih.webauthn.domain.WebAuthnCredentialsRepository;
import com.mih.webauthn.domain.WebAuthnUser;
import com.mih.webauthn.domain.WebAuthnUserRepository;
import com.mih.webauthn.service.DefaultCredentialService;
import com.yubico.webauthn.RelyingParty;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WebauthnConfigurer extends AbstractHttpConfigurer<WebauthnConfigurer, HttpSecurity> {

    private Consumer<WebAuthnUser> successHandler = (user) -> {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(token);
    };
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

    public WebauthnConfigurer successHandler(Consumer<WebAuthnUser> successHandler) {
        Assert.notNull(successHandler, "successHandler cannot be null");
        this.successHandler = successHandler;
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

        this.filter.registerDefaults(getBean(http, WebAuthnUserRepository.class),
                getBean(http, WebAuthnCredentialsRepository.class),
                getBean(http, DefaultCredentialService.class),
                getBean(http, RelyingParty.class),
                getBean(http, ObjectMapper.class));

        this.filter.setSuccessHandler(successHandler);
        this.filter.setUserSupplier(userSupplier);

        http.addFilterBefore(filter, BasicAuthenticationFilter.class);
    }

    private <T> T getBean(HttpSecurity http, Class<T> clasz) {
        return http.getSharedObject(ApplicationContext.class).getBean(clasz);
    }


}
