package io.github.webauthn.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.webauthn.RelyingParty;
import io.github.webauthn.WebAuthnFilter;
import io.github.webauthn.WebAuthnProperties;
import io.github.webauthn.domain.WebAuthnCredentials;
import io.github.webauthn.domain.WebAuthnCredentialsRepository;
import io.github.webauthn.domain.WebAuthnUser;
import io.github.webauthn.domain.WebAuthnUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * WebAuthentication configurer adding the {@link WebAuthnFilter} on the {@link WebAuthnProperties#getEndpoints()} paths.
 * <p>When an user is authenticated the {@link WebAuthnConfigurer#loginSuccessHandler} is called.
 * You can override this to set your own {@link org.springframework.security.core.Authentication} implementation</p>
 * <p/>
 * <pre>
 *     UsernamePasswordAuthenticationToken token = new WebAuthnUsernameAuthenticationToken(user, credentials, Collections.emptyList());
 *     SecurityContextHolder.getContext().setAuthentication(token);
 * </pre>
 *
 * <p>You can receive the newly registered users via the {@link WebAuthnConfigurer#registerSuccessHandler}</p>
 * <p></p>
 * <pre>
 *     http
 * .apply(new WebAuthnConfigurer()
 * .defaultLoginSuccessHandler((user, credentials) -> log.info("user logged in: {}", user))
 * .registerSuccessHandler(user -> {
 * log.info("new user registered: {}", user);
 * })
 * </pre>
 * <p><b>Registration</b></p>
 * <ul>
 * <li><i>/registration/start</i> - returns the public key creation options linked to a {@link WebAuthnUser}.
 * Depending on the flow, this can be a new user, an user identified by a recovery token
 * or an user identified by a registration add token from the add device flow</li>
 * <li><i>/registration/finish</i> - receives the signed challenge and saves the new credentials</li>
 * </ul>
 *
 * <p><b>Authentication</b></p>
 * <ul>
 * <li><i>/assertion/start</i> - returns an assertion request for the authenticator to sign</li>
 * <li><i>/assertion/finish</i> - receives the assertion result and calls the {@link WebAuthnConfigurer#loginSuccessHandler}</li>
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
    private BiConsumer<WebAuthnUser, WebAuthnCredentials> loginSuccessHandler = (user, credentials) -> {
        UsernamePasswordAuthenticationToken token = new WebAuthnUsernameAuthenticationToken(user, credentials, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(token);
    };
    private Consumer<WebAuthnUser> registerSuccessHandler;

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
     *     };
     * </pre>
     *
     * @param successHandler
     * @return
     */
    public WebAuthnConfigurer loginSuccessHandler(BiConsumer<WebAuthnUser, WebAuthnCredentials> successHandler) {
        Assert.notNull(successHandler, "successHandler cannot be null");
        this.loginSuccessHandler = successHandler;
        return this;
    }

    /**
     * Use the default {@link #loginSuccessHandler} that updates the {@link org.springframework.security.core.context.SecurityContext}
     * and cascade your own handler afterwards
     *
     * @param andThen
     * @return
     * @see #loginSuccessHandler
     */
    public WebAuthnConfigurer defaultLoginSuccessHandler(BiConsumer<WebAuthnUser, WebAuthnCredentials> andThen) {
        Assert.notNull(andThen, "andThen cannot be null");
        this.loginSuccessHandler = loginSuccessHandler.andThen(andThen);
        return this;
    }

    /**
     * Use this method to get the newly registered user
     *
     * @param registerSuccessHandler
     * @return
     */
    public WebAuthnConfigurer registerSuccessHandler(Consumer<WebAuthnUser> registerSuccessHandler) {
        Assert.notNull(registerSuccessHandler, "registerSuccessHandler cannot be null");
        this.registerSuccessHandler = registerSuccessHandler;
        return this;
    }

    /**
     * In the add device flow, we need the currently authenticated user to set a new registrationAddToken
     * that can be used on user's new device.
     *
     * @param userSupplier
     * @return
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
                getBean(http, WebAuthnOperation.class)
        );

        this.filter.setSuccessHandler(loginSuccessHandler);
        this.filter.setUserSupplier(userSupplier);
        this.filter.setRegisterSuccessHandler(registerSuccessHandler);

        http.addFilterBefore(filter, BasicAuthenticationFilter.class);
    }

    private <T> T getBean(HttpSecurity http, Class<T> clasz) {
        return http.getSharedObject(ApplicationContext.class).getBean(clasz);
    }

}
