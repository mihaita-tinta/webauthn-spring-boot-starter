package com.mih.webauthn.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mih.webauthn.domain.AppUser;
import com.mih.webauthn.repository.AppCredentialsRepository;
import com.mih.webauthn.repository.AppUserRepository;
import com.mih.webauthn.service.CredentialService;
import com.mih.webauthn.dto.*;
import com.yubico.webauthn.RelyingParty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

public class WebAuthnFilter extends GenericFilterBean {
    private static final Logger logger = LoggerFactory.getLogger(WebAuthnFilter.class);
    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REGISTRATION_START_REQUEST_MATCHER = new AntPathRequestMatcher("/registration/start", "POST");
    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REGISTRATION_FINISH_REQUEST_MATCHER = new AntPathRequestMatcher("/registration/finish", "POST");
    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_ASSERTION_START_REQUEST_MATCHER = new AntPathRequestMatcher("/assertion/start", "POST");
    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_ASSERTION_FINISH_REQUEST_MATCHER = new AntPathRequestMatcher("/assertion/finish", "POST");

    private RequestMatcher registrationStartPath;
    private RequestMatcher registrationFinishPath;
    private RequestMatcher assertionStartPath;
    private RequestMatcher assertionFinishPath;
    private WebAuthnRegistrationStartStrategy startStrategy;
    private WebAuthnRegistrationFinishStrategy finishStrategy;
    private WebAuthnAssertionStartStrategy assertionStartStrategy;
    private WebAuthnAssertionFinishStrategy assertionFinishStrategy;
    private SuccessHandler successHandler;
    private ObjectMapper mapper;

    public WebAuthnFilter(AppUserRepository appUserRepository, AppCredentialsRepository credentialRepository, CredentialService credentialService, RelyingParty relyingParty, ObjectMapper mapper) {
        this.registrationStartPath = DEFAULT_ANT_PATH_REGISTRATION_START_REQUEST_MATCHER;
        this.registrationFinishPath = DEFAULT_ANT_PATH_REGISTRATION_FINISH_REQUEST_MATCHER;
        this.assertionStartPath = DEFAULT_ANT_PATH_ASSERTION_START_REQUEST_MATCHER;
        this.assertionFinishPath = DEFAULT_ANT_PATH_ASSERTION_FINISH_REQUEST_MATCHER;
        this.mapper = mapper;
        InMemoryOperation<RegistrationStartResponse> registrationOperation = new InMemoryOperation();
        this.startStrategy = new WebAuthnRegistrationStartStrategy(appUserRepository,
                credentialRepository, relyingParty, registrationOperation);
        this.finishStrategy = new WebAuthnRegistrationFinishStrategy(appUserRepository,
                credentialRepository, relyingParty, registrationOperation);

        InMemoryOperation<AssertionStartResponse> assertionOperation = new InMemoryOperation();
        this.assertionStartStrategy = new WebAuthnAssertionStartStrategy(appUserRepository,
                credentialRepository, relyingParty, assertionOperation);
        this.assertionFinishStrategy = new WebAuthnAssertionFinishStrategy(appUserRepository,
                credentialService, relyingParty, assertionOperation);
    }

    public SuccessHandler getSuccessHandler() {
        return successHandler;
    }

    public void setSuccessHandler(SuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        if (this.registrationStartPath.matches(req)) {
            RegistrationStartRequest body = mapper.readValue(request.getReader(), RegistrationStartRequest.class);
            RegistrationStartResponse registrationStartResponse = startStrategy.registrationStart(body);
            HttpServletResponse res = (HttpServletResponse) response;
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write(mapper.writeValueAsString(registrationStartResponse));
            res.getWriter().flush();
        } else if (this.registrationFinishPath.matches(req)) {
            RegistrationFinishRequest body = mapper.readValue(request.getReader(), RegistrationFinishRequest.class);
            String ok = finishStrategy.registrationFinish(body);
            HttpServletResponse res = (HttpServletResponse) response;
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write(ok);
            res.getWriter().flush();
        } else if (assertionStartPath.matches(req)) {
            String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            AssertionStartResponse start = assertionStartStrategy.start(body);
            HttpServletResponse res = (HttpServletResponse) response;
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write(mapper.writeValueAsString(start));
            res.getWriter().flush();
        } else if (assertionFinishPath.matches(req)) {
            AssertionFinishRequest body = mapper.readValue(request.getReader(), AssertionFinishRequest.class);
            Optional<AppUser> user = assertionFinishStrategy.finish(body);
            user.ifPresent(u -> successHandler.onUser(u));
            HttpServletResponse res = (HttpServletResponse) response;
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write(mapper.writeValueAsString(user.isPresent()));
            res.getWriter().flush();
        }
        chain.doFilter(request, response);
    }
}
