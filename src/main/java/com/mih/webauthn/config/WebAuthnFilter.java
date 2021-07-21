package com.mih.webauthn.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mih.webauthn.domain.WebAuthnCredentialsRepository;
import com.mih.webauthn.domain.WebAuthnUser;
import com.mih.webauthn.domain.WebAuthnUserRepository;
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
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class WebAuthnFilter extends GenericFilterBean {
    private static final Logger logger = LoggerFactory.getLogger(WebAuthnFilter.class);
    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REGISTRATION_START_REQUEST_MATCHER = new AntPathRequestMatcher("/registration/start", "POST");
    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REGISTRATION_ADD_REQUEST_MATCHER = new AntPathRequestMatcher("/registration/add", "GET");
    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REGISTRATION_FINISH_REQUEST_MATCHER = new AntPathRequestMatcher("/registration/finish", "POST");
    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_ASSERTION_START_REQUEST_MATCHER = new AntPathRequestMatcher("/assertion/start", "POST");
    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_ASSERTION_FINISH_REQUEST_MATCHER = new AntPathRequestMatcher("/assertion/finish", "POST");

    private RequestMatcher registrationStartPath;
    private RequestMatcher registrationFinishPath;
    private RequestMatcher registrationAddPath;
    private RequestMatcher assertionStartPath;
    private RequestMatcher assertionFinishPath;
    private WebAuthnRegistrationStartStrategy startStrategy;
    private WebAuthnRegistrationAddStrategy addStrategy;
    private WebAuthnRegistrationFinishStrategy finishStrategy;
    private WebAuthnAssertionStartStrategy assertionStartStrategy;
    private WebAuthnAssertionFinishStrategy assertionFinishStrategy;
    private Consumer<WebAuthnUser> successHandler;
    private Supplier<WebAuthnUser> userSupplier;
    private ObjectMapper mapper;

    public WebAuthnFilter() {

    }

    public void registerDefaults(WebAuthnUserRepository appUserRepository, WebAuthnCredentialsRepository credentialRepository, RelyingParty relyingParty, ObjectMapper mapper) {
        this.registrationStartPath = DEFAULT_ANT_PATH_REGISTRATION_START_REQUEST_MATCHER;
        this.registrationAddPath = DEFAULT_ANT_PATH_REGISTRATION_ADD_REQUEST_MATCHER;
        this.registrationFinishPath = DEFAULT_ANT_PATH_REGISTRATION_FINISH_REQUEST_MATCHER;
        this.assertionStartPath = DEFAULT_ANT_PATH_ASSERTION_START_REQUEST_MATCHER;
        this.assertionFinishPath = DEFAULT_ANT_PATH_ASSERTION_FINISH_REQUEST_MATCHER;
        this.mapper = mapper;
        InMemoryOperation<RegistrationStartResponse> registrationOperation = new InMemoryOperation();
        this.startStrategy = new WebAuthnRegistrationStartStrategy(appUserRepository,
                credentialRepository, relyingParty, registrationOperation);
        this.addStrategy = new WebAuthnRegistrationAddStrategy(appUserRepository);
        this.finishStrategy = new WebAuthnRegistrationFinishStrategy(appUserRepository,
                credentialRepository, relyingParty, registrationOperation);

        InMemoryOperation<AssertionStartResponse> assertionOperation = new InMemoryOperation();
        this.assertionStartStrategy = new WebAuthnAssertionStartStrategy(relyingParty, assertionOperation);
        this.assertionFinishStrategy = new WebAuthnAssertionFinishStrategy(appUserRepository,
                credentialRepository, relyingParty, assertionOperation);
    }

    public Consumer<WebAuthnUser> getSuccessHandler() {
        return successHandler;
    }

    public void setSuccessHandler(Consumer<WebAuthnUser> successHandler) {
        this.successHandler = successHandler;
    }

    public void setRegisterSuccessHandler(Consumer<WebAuthnUser> registerSuccessHandler) {
        this.finishStrategy.setRegisterSuccessHandler(registerSuccessHandler);
    }

    public void setUserSupplier(Supplier<WebAuthnUser> userSupplier) {
        this.userSupplier = userSupplier;
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
            writeToResponse(response, mapper.writeValueAsString(registrationStartResponse));
        } else if (this.registrationFinishPath.matches(req)) {
            RegistrationFinishRequest body = mapper.readValue(request.getReader(), RegistrationFinishRequest.class);
            String ok = finishStrategy.registrationFinish(body);
            writeToResponse(response, ok);
        } else if (this.registrationAddPath.matches(req)) {
            String addToken = addStrategy.registrationAdd(userSupplier.get());
            writeToResponse(response, addToken);
        } else if (assertionStartPath.matches(req)) {
            String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            AssertionStartResponse start = assertionStartStrategy.start(body);
            writeToResponse(response, mapper.writeValueAsString(start));
        } else if (assertionFinishPath.matches(req)) {
            AssertionFinishRequest body = mapper.readValue(request.getReader(), AssertionFinishRequest.class);
            Optional<WebAuthnUser> user = assertionFinishStrategy.finish(body);
            user.ifPresent(u -> successHandler.accept(u));
            writeToResponse(response, mapper.writeValueAsString(user.isPresent()));
        } else {
            chain.doFilter(request, response);
        }
    }

    private void writeToResponse(ServletResponse response, String body) throws IOException {
        HttpServletResponse res = (HttpServletResponse) response;
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write(body);
        res.getWriter().flush();
    }
}
