package io.github.webauthn.webflux;

import io.github.webauthn.BytesUtil;
import io.github.webauthn.domain.*;
import io.github.webauthn.dto.AssertionStartRequest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest(classes = {SpringWebFluxTestConfig.class, WebAuthnWebFluxConfig.class},
        properties = "spring.main.web-application-type=reactive")
@AutoConfigureWebTestClient
class WebAuthnWebFluxConfigTest {
    private static final Logger log = LoggerFactory.getLogger(WebAuthnWebFluxConfigTest.class);

    @Autowired
    private WebTestClient client;

    @Autowired
    WebAuthnUserRepository<WebAuthnUser> webAuthnUserRepository;
    @Autowired
    WebAuthnCredentialsRepository<WebAuthnCredentials> credentialsRepository;

    @Test
    public void testStart() {

        DefaultWebAuthnUser user = new DefaultWebAuthnUser();
        user.setUsername("junit");
        webAuthnUserRepository.save(user);

        DefaultWebAuthnCredentials credentials = new DefaultWebAuthnCredentials();
        credentials.setAppUserId(user.getId());
        credentials.setCredentialId(BytesUtil.longToBytes(123L));
        credentialsRepository.save(credentials);

        AssertionStartRequest request = new AssertionStartRequest();
        request.setUsername("junit");

        client
                .post()
                .uri("/assertion/start")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(request), AssertionStartRequest.class)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(s -> log.info(s.toString()))
                .jsonPath("$.assertionId").exists()
                .jsonPath("$.publicKeyCredentialRequestOptions").exists();
    }

    @Test
    public void testUnauthorized() {

        AssertionStartRequest request = new AssertionStartRequest();
        request.setUsername("not-existing");

        client
                .post()
                .uri("/assertion/start")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(request), AssertionStartRequest.class)
                .exchange()
                .expectStatus()
                .isUnauthorized();

    }

}
