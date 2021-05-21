package com.mih.webauthn.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mih.webauthn.repository.AppCredentialsRepository;
import com.mih.webauthn.repository.AppUserRepository;
import com.mih.webauthn.service.CredentialService;
import com.yubico.webauthn.RelyingParty;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

public class WebauthnConfigurer extends AbstractHttpConfigurer<WebauthnConfigurer, HttpSecurity> {

    private final AppUserRepository appUserRepository;
    private final AppCredentialsRepository credentialRepository;
    private final CredentialService credentialService;
    private final RelyingParty relyingParty;
    private final ObjectMapper mapper;
    private SuccessHandler successHandler;
    private WebAuthnFilter filter;

    public WebauthnConfigurer(AppUserRepository appUserRepository, AppCredentialsRepository credentialRepository, CredentialService credentialService, RelyingParty relyingParty, ObjectMapper mapper) {
        this.appUserRepository = appUserRepository;
        this.credentialRepository = credentialRepository;
        this.credentialService = credentialService;
        this.relyingParty = relyingParty;
        this.mapper = mapper;
        this.filter = new WebAuthnFilter(appUserRepository, credentialRepository, credentialService, relyingParty, mapper);
    }

    @Override
    public void init(HttpSecurity http) {
        // initialization code
    }
    public WebauthnConfigurer successHandler(SuccessHandler successHandler) {
        this.filter.setSuccessHandler(successHandler);
        return this;
    }

    @Override
    public void configure(HttpSecurity http) {
        http.addFilterBefore(filter, FilterSecurityInterceptor.class);
    }
}
