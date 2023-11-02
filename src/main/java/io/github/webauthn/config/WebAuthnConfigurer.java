package io.github.webauthn.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.webauthn.RelyingParty;
import io.github.webauthn.WebAuthnFilter;
import io.github.webauthn.WebAuthnProperties;
import io.github.webauthn.domain.WebAuthnCredentials;
import io.github.webauthn.domain.WebAuthnCredentialsRepository;
import io.github.webauthn.domain.WebAuthnUser;
import io.github.webauthn.domain.WebAuthnUserRepository;
import io.github.webauthn.events.WebAuthnEventPublisher;
import io.github.webauthn.flows.WebAuthnAssertionFinishStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * WebAuthentication configurer adding the {@link WebAuthnFilter} on the {@link WebAuthnProperties#getEndpoints()} paths.
 * <p>When an user is authenticated the {@link WebAuthnConfigurer#updateSecurityContextHandler} is called.
 * You can override this to set your own {@link org.springframework.security.core.Authentication} implementation</p>
 * <p>&nbsp;</p>
 * <pre>
 *     (user, credentials) -> {
 *         UsernamePasswordAuthenticationToken token = new WebAuthnUsernameAuthenticationToken(user, credentials, Collections.emptyList());
 *         SecurityContextHolder.getContext().setAuthentication(token);
 *         ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
 *         attr.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext(), RequestAttributes.SCOPE_SESSION);
 *     };
 * </pre>
 *
 * <p>You can receive the newly registered users via an {@link org.springframework.context.event.EventListener}
 * catching an {@link io.github.webauthn.events.NewRecoveryTokenCreated} event</p>
 * <p>&nbsp;</p>
 * <p><b>Registration</b></p>
 * <ul>
 * <li><i>/registration/start</i> - returns the public key creation options linked to a {@link WebAuthnUser}.
 * Depending on the flow, this can be:
 * a new user - dictated by {@link WebAuthnConfigurer#userSupplier},
 * an user identified by a recovery token,
 * or an user identified by a registration add token from the add device flow</li>
 * <li><i>/registration/finish</i> - receives the signed challenge and saves the new credentials</li>
 * </ul>
 *
 * <p><b>Authentication</b></p>
 * <ul>
 * <li><i>/assertion/start</i> - returns an assertion request for the authenticator to sign</li>
 * <li><i>/assertion/finish</i> - receives the assertion result and calls the {@link WebAuthnConfigurer#updateSecurityContextHandler}</li>
 * </ul>
 * <p><b>Add device</b></p>
 * <ul>
 * <li><i>/registration/add</i> - for a given authenticated user, set a new registration add token with a limited lifespan (10 min)
 * The {@link WebAuthnConfigurer#userSupplier} provides the current authenticated user which you can override
 * </li>
 * </ul>
 */
public class WebAuthnConfigurer extends AbstractHttpConfigurer<WebAuthnConfigurer, HttpSecurity> {

    private static final Logger log = LoggerFactory.getLogger(WebAuthnConfigurer.class);
    private BiConsumer<WebAuthnUser, WebAuthnCredentials> updateSecurityContextHandler = (user, credentials) -> {
        UsernamePasswordAuthenticationToken token = new WebAuthnUsernameAuthenticationToken(user, credentials, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(token);
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        attr.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext(), RequestAttributes.SCOPE_SESSION);
    };

    private Function<WebAuthnAssertionFinishStrategy.AssertionSuccessResponse, Object> authenticationSuccessResponseMapper = (finish) ->
            Map.of("username", finish.getUser().getUsername());

    /**
     * Default supplier used to retrieve user information from the {@link SecurityContextHolder}
     */
    private Supplier<WebAuthnUser> userSupplier = () -> {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        if (token == null)
            return null;

        Object principal = token.getPrincipal();
        if (principal instanceof WebAuthnUser) {
            return (WebAuthnUser) principal;
        } else {
            log.warn("userSupplier - you need to configure your WebAuthnConfigurer.userSupplier method to tranform your principal implementation to something that webauthn starter can understand");
        }
        return null;
    };

    private WebAuthnFilter filter;

    public WebAuthnConfigurer() {
    }

    @Override
    public void init(HttpSecurity http) {
    }

    /**
     * Use this method to customize what is added to the {@link org.springframework.security.core.context.SecurityContextHolder}
     * <pre>
     *     (user, credentials) -> {
     *         UsernamePasswordAuthenticationToken token = new WebAuthnUsernameAuthenticationToken(user, credentials, Collections.emptyList());
     *         SecurityContextHolder.getContext().setAuthentication(token);
     *         ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
     *         attr.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext(), RequestAttributes.SCOPE_SESSION);
     *     };
     * </pre>
     *
     * @param successHandler
     * @return
     */
    public WebAuthnConfigurer updateSecurityContextHandler(BiConsumer<WebAuthnUser, WebAuthnCredentials> successHandler) {
        Assert.notNull(successHandler, "updateSecurityContextHandler cannot be null");
        this.updateSecurityContextHandler = successHandler;
        return this;
    }

    /**
     * By default we return the username on successful authentication attempts:
     * <pre>
     *     {
     *         "username": "name"
     *     }
     * </pre>
     *
     * @return
     */
    public WebAuthnConfigurer authenticationSuccessResponseMapper(Function<WebAuthnAssertionFinishStrategy.AssertionSuccessResponse, Object> authenticationSuccessHandler) {
        Assert.notNull(authenticationSuccessHandler, "authenticationSuccessResponseMapper cannot be null");
        this.authenticationSuccessResponseMapper = authenticationSuccessHandler;
        return this;
    }

    /**
     * Use the default {@link #updateSecurityContextHandler} to update the {@link org.springframework.security.core.context.SecurityContext} with a default {@link WebAuthnUsernameAuthenticationToken}
     * and cascade your own handler afterwards
     *
     * @param andThen
     * @return
     * @see #updateSecurityContextHandler
     */
    public WebAuthnConfigurer defaultLoginSuccessHandler(BiConsumer<WebAuthnUser, WebAuthnCredentials> andThen) {
        Assert.notNull(andThen, "andThen cannot be null");
        this.updateSecurityContextHandler = updateSecurityContextHandler.andThen(andThen);
        return this;
    }

    /**
     * In the add device flow, we need the currently authenticated user to set a new registrationAddToken
     * that can be used on user's new device.
     *
     * @param userSupplier
     * @return WebAuthnConfigurer
     * @see io.github.webauthn.flows.WebAuthnRegistrationAddStrategy
     */
    public WebAuthnConfigurer userSupplier(Supplier<WebAuthnUser> userSupplier) {
        Assert.notNull(userSupplier, "userSupplier cannot be null");
        this.userSupplier = userSupplier;
        return this;
    }

    @Override
    public void configure(HttpSecurity http) {

        this.filter = new WebAuthnFilter();

        this.filter.registerDefaults(
                getBean(http, WebAuthnProperties.class),
                getBean(http, WebAuthnUserRepository.class),
                getBean(http, WebAuthnCredentialsRepository.class),
                getBean(http, RelyingParty.class),
                getBean(http, ObjectMapper.class),
                getBean(http, WebAuthnOperation.class),
                getBean(http, WebAuthnOperation.class),
                getBean(http, WebAuthnEventPublisher.class)
        );

        this.filter.setUpdateSecurityContextHandler(updateSecurityContextHandler);
        this.filter.setAuthenticationSuccessHandler(authenticationSuccessResponseMapper);
        this.filter.setUserSupplier(userSupplier);

        http.addFilterBefore(filter, BasicAuthenticationFilter.class);
    }

    private <T> T getBean(HttpSecurity http, Class<T> clasz) {
        return http.getSharedObject(ApplicationContext.class).getBean(clasz);
    }

}
