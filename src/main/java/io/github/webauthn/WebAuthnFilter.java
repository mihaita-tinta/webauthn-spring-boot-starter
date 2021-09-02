package io.github.webauthn;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.webauthn.RelyingParty;
import io.github.webauthn.config.WebAuthnOperation;
import io.github.webauthn.domain.WebAuthnCredentials;
import io.github.webauthn.domain.WebAuthnCredentialsRepository;
import io.github.webauthn.domain.WebAuthnUser;
import io.github.webauthn.domain.WebAuthnUserRepository;
import io.github.webauthn.dto.*;
import io.github.webauthn.flows.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

public class WebAuthnFilter extends GenericFilterBean {
    private static final Logger log = LoggerFactory.getLogger(WebAuthnFilter.class);

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

    public void registerDefaults(WebAuthnProperties properties, WebAuthnUserRepository appUserRepository,
                                 WebAuthnCredentialsRepository credentialRepository, RelyingParty relyingParty, ObjectMapper mapper,
                                 WebAuthnOperation<RegistrationStartResponse, String> registrationOperation,
                                 WebAuthnOperation<AssertionStartResponse, String> assertionOperation) {
        this.registrationStartPath = properties.getEndpoints().getRegistrationStartPath();
        this.registrationAddPath = properties.getEndpoints().getRegistrationAddPath();
        this.registrationFinishPath = properties.getEndpoints().getRegistrationFinishPath();
        this.assertionStartPath = properties.getEndpoints().getAssertionStartPath();
        this.assertionFinishPath = properties.getEndpoints().getAssertionFinishPath();
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
        try {
            if (this.registrationStartPath.matches(req)) {
                RegistrationStartRequest body = parseRequest(request, RegistrationStartRequest.class);
                try {
                    RegistrationStartResponse registrationStartResponse = startStrategy.registrationStart(body);
                    String json = mapper.writeValueAsString(registrationStartResponse);

                    writeToResponse(response, json);
                } catch (UsernameAlreadyExistsException e) {
                    writeBadRequestToResponse(response, new RegistrationStartResponse(RegistrationStartResponse.Status.USERNAME_TAKEN));
                } catch (InvalidTokenException e) {
                    writeBadRequestToResponse(response, new RegistrationStartResponse(RegistrationStartResponse.Status.TOKEN_INVALID));
                }

            } else if (this.registrationFinishPath.matches(req)) {
                RegistrationFinishRequest body = parseRequest(request, RegistrationFinishRequest.class);
                Map<String, String> map = finishStrategy.registrationFinish(body);
                String json = mapper.writeValueAsString(map);
                writeToResponse(response, json);

            } else if (this.registrationAddPath.matches(req)) {
                Map<String, String> map = addStrategy.registrationAdd(userSupplier.get());
                String json = mapper.writeValueAsString(map);
                writeToResponse(response, json);

            } else if (assertionStartPath.matches(req)) {
                AssertionStartRequest startRequest = parseRequest(request, AssertionStartRequest.class);
                try {
                    AssertionStartResponse start = assertionStartStrategy.start(startRequest);
                    String json = mapper.writeValueAsString(start);
                    writeToResponse(response, json);
                } catch (UsernameNotFoundException e) {
                    writeBadRequestToResponse(response, Map.of("message", e.getMessage()));
                }

            } else if (assertionFinishPath.matches(req)) {
                AssertionFinishRequest body = parseRequest(request, AssertionFinishRequest.class);
                Optional<WebAuthnAssertionFinishStrategy.AssertionSuccessResponse> res = assertionFinishStrategy.finish(body);
                res.ifPresent(u -> successHandler.accept(u.getUser(), u.getCredentials()));
                writeToResponse(response, mapper.writeValueAsString(Map.of("username", res.get().getUser().getUsername())));
            } else {
                chain.doFilter(request, response);
            }
        } catch (JsonParseException e) {
            writeBadRequestToResponse(response, Map.of("message", e.getMessage()));
        }
    }

    private <T> T parseRequest(ServletRequest request, Class<T> clasz) throws IOException {
        return mapper.readValue(request.getReader(), clasz);
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
