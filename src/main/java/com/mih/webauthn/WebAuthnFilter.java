package com.mih.webauthn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mih.webauthn.config.InMemoryOperation;
import com.mih.webauthn.config.WebAuthnOperation;
import com.mih.webauthn.domain.WebAuthnCredentials;
import com.mih.webauthn.domain.WebAuthnCredentialsRepository;
import com.mih.webauthn.domain.WebAuthnUser;
import com.mih.webauthn.domain.WebAuthnUserRepository;
import com.mih.webauthn.dto.*;
import com.mih.webauthn.flows.*;
import com.yubico.webauthn.RelyingParty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class WebAuthnFilter extends GenericFilterBean {
    private static final Logger log = LoggerFactory.getLogger(WebAuthnFilter.class);
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
    private BiConsumer<WebAuthnUser, WebAuthnCredentials> successHandler;
    private Supplier<WebAuthnUser> userSupplier;
    private ObjectMapper mapper;

    public WebAuthnFilter() {

    }

    public void registerDefaults(WebAuthnUserRepository appUserRepository, WebAuthnCredentialsRepository credentialRepository, RelyingParty relyingParty, ObjectMapper mapper,
                                 WebAuthnOperation<RegistrationStartResponse, String> registrationOperation,
                                 WebAuthnOperation<AssertionStartResponse, String> assertionOperation) {
        this.registrationStartPath = DEFAULT_ANT_PATH_REGISTRATION_START_REQUEST_MATCHER;
        this.registrationAddPath = DEFAULT_ANT_PATH_REGISTRATION_ADD_REQUEST_MATCHER;
        this.registrationFinishPath = DEFAULT_ANT_PATH_REGISTRATION_FINISH_REQUEST_MATCHER;
        this.assertionStartPath = DEFAULT_ANT_PATH_ASSERTION_START_REQUEST_MATCHER;
        this.assertionFinishPath = DEFAULT_ANT_PATH_ASSERTION_FINISH_REQUEST_MATCHER;
        this.mapper = mapper;


        this.startStrategy = new WebAuthnRegistrationStartStrategy(appUserRepository,
                credentialRepository, relyingParty, registrationOperation);
        this.addStrategy = new WebAuthnRegistrationAddStrategy(appUserRepository);
        this.finishStrategy = new WebAuthnRegistrationFinishStrategy(appUserRepository,
                credentialRepository, relyingParty, registrationOperation);

        this.assertionStartStrategy = new WebAuthnAssertionStartStrategy(relyingParty, assertionOperation);
        this.assertionFinishStrategy = new WebAuthnAssertionFinishStrategy(appUserRepository,
                credentialRepository, relyingParty, assertionOperation);
    }

    public BiConsumer<WebAuthnUser, WebAuthnCredentials> getSuccessHandler() {
        return successHandler;
    }

    public void setSuccessHandler(BiConsumer<WebAuthnUser, WebAuthnCredentials> successHandler) {
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
            try {
                RegistrationStartResponse registrationStartResponse = startStrategy.registrationStart(body);
                String json = mapper.writeValueAsString(registrationStartResponse);

                log.debug("doFilter - registrationStartPath json: {}", json);

                writeToResponse(response, json);
            } catch (UsernameAlreadyExistsException e) {
                writeBadRequestToResponse(response, new RegistrationStartResponse(RegistrationStartResponse.Status.USERNAME_TAKEN));
            }catch (InvalidTokenException e) {
                writeBadRequestToResponse(response, new RegistrationStartResponse(RegistrationStartResponse.Status.TOKEN_INVALID));
            }

        } else if (this.registrationFinishPath.matches(req)) {
            RegistrationFinishRequest body = mapper.readValue(request.getReader(), RegistrationFinishRequest.class);
            Map<String, String> map = finishStrategy.registrationFinish(body);
            String json = mapper.writeValueAsString(map);
            log.debug("doFilter - registrationFinishPath json: {}", json);
            writeToResponse(response, json);

        } else if (this.registrationAddPath.matches(req)) {
            Map<String, String> map = addStrategy.registrationAdd(userSupplier.get());
            String json = mapper.writeValueAsString(map);
            log.debug("doFilter - registrationAddPath addToken: {}", json);
            writeToResponse(response, json);

        } else if (assertionStartPath.matches(req)) {
            String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            try {
                AssertionStartResponse start = assertionStartStrategy.start(body);
                String json = mapper.writeValueAsString(start);
                log.debug("doFilter - assertionStartPath json: {}", json);
                writeToResponse(response, json);
            } catch (UsernameNotFoundException e) {
                writeBadRequestToResponse(response, Map.of("message", e.getMessage()));
            }

        } else if (assertionFinishPath.matches(req)) {
            AssertionFinishRequest body = mapper.readValue(request.getReader(), AssertionFinishRequest.class);
            Optional<WebAuthnAssertionFinishStrategy.AssertionSuccessResponse> res = assertionFinishStrategy.finish(body);
            log.debug("doFilter - assertionFinishPath found user: {}", res);
            res.ifPresent(u -> successHandler.accept(u.getUser(), u.getCredentials()));
            writeToResponse(response, mapper.writeValueAsString(Map.of("username", res.get().getUser().getUsername())));
        } else {
            chain.doFilter(request, response);
        }
    }

    private void writeToResponse(ServletResponse response, String body) throws IOException {
        writeToResponse(HttpServletResponse.SC_OK, response, body);
    }


    private void writeBadRequestToResponse(ServletResponse response, RegistrationStartResponse body) throws IOException {
        writeToResponse(HttpServletResponse.SC_BAD_REQUEST, response, mapper.writeValueAsString(body));
    }
    private void writeBadRequestToResponse(ServletResponse response, Map<String, String> body) throws IOException {
        writeToResponse(HttpServletResponse.SC_BAD_REQUEST, response, mapper.writeValueAsString(body));
    }

    private void writeToResponse(int status, ServletResponse response, String body) throws IOException {
        log.debug("writeToResponse - status: {}, body: {}", status, body);
        HttpServletResponse res = (HttpServletResponse) response;
        res.setStatus(status);
        res.getWriter().write(body);
        res.getWriter().flush();
    }
}
