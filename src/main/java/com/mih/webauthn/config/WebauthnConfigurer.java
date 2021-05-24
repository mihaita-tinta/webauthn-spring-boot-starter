package com.mih.webauthn.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mih.webauthn.repository.WebAuthnCredentialsRepository;
import com.mih.webauthn.repository.WebAuthnUserRepository;
import com.mih.webauthn.service.DefaultCredentialService;
import com.yubico.webauthn.RelyingParty;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.util.Assert;

public class WebauthnConfigurer extends AbstractHttpConfigurer<WebauthnConfigurer, HttpSecurity> {

    private SuccessHandler successHandler;
    private WebAuthnFilter filter;

    public WebauthnConfigurer() {
    }

    @Override
    public void init(HttpSecurity http) {
    }

    public WebauthnConfigurer successHandler(SuccessHandler successHandler) {
        Assert.notNull(successHandler, "successHandler cannot be null");
        this.successHandler = successHandler;
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

        http.addFilterBefore(filter, FilterSecurityInterceptor.class);
    }

    private <T> T getBean(HttpSecurity http, Class<T> clasz) {
        return http.getSharedObject(ApplicationContext.class).getBean(clasz);
    }


}
