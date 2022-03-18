package io.github.webauthn.webflux;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.webauthn.AssertionRequest;
import io.github.webauthn.BytesUtil;
import io.github.webauthn.JsonConfig;
import io.github.webauthn.config.WebAuthnOperation;
import io.github.webauthn.domain.*;
import io.github.webauthn.dto.AssertionStartRequest;
import io.github.webauthn.dto.AssertionStartResponse;
import io.github.webauthn.dto.RegistrationStartRequest;
import io.github.webauthn.jpa.JpaWebAuthnCredentials;
import io.github.webauthn.jpa.JpaWebAuthnUser;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {SpringWebFluxTestConfig.class, JsonConfig.class, WebAuthnWebFluxConfig.class},
        properties = {
                "webauthn.relyingPartyId=localhost",
                "webauthn.relyingPartyName=localhost",
                "webauthn.relyingPartyOrigins=http://localhost:8080",
                "spring.main.web-application-type=reactive"})
@AutoConfigureWebTestClient
class WebAuthnWebFilterTest {
    private static final Logger log = LoggerFactory.getLogger(WebAuthnWebFluxConfigTest.class);

    @Autowired
    private WebTestClient client;

    @Autowired
    WebAuthnUserRepository webAuthnUserRepository;
    @Autowired
    WebAuthnCredentialsRepository credentialsRepository;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    WebAuthnOperation assertionOperation;

    @Test
    public void testUnauthorized() {

        AssertionStartRequest request = new AssertionStartRequest();
        request.setUsername("not-found");

        client
                .post()
                .uri("/assertion/start")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(request), AssertionStartRequest.class)
                .exchange()
                .expectStatus()
                .isUnauthorized();

    }

    @Test
    public void testStart() {

        JpaWebAuthnUser user = new JpaWebAuthnUser();
        user.setUsername("user-start");
        webAuthnUserRepository.save(user);

        JpaWebAuthnCredentials credentials = new JpaWebAuthnCredentials();
        credentials.setAppUserId(user.getId());
        credentials.setCredentialId(BytesUtil.longToBytes(123L));
        credentialsRepository.save(credentials);

        AssertionStartRequest request = new AssertionStartRequest();
        request.setUsername("user-start");

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
    public void testAssertionFinish() throws JsonProcessingException {

        JpaWebAuthnUser user = new JpaWebAuthnUser();
        user.setUsername("junit");
        WebAuthnUser saved = webAuthnUserRepository.save(user);

        JpaWebAuthnCredentials credentials = new JpaWebAuthnCredentials();
        credentials.setAppUserId(saved.getId());
        credentials.setCredentialId(Base64.getDecoder().decode("ARgxyHfw5N83gRMl2M7vHhqkQmtHwDJ8QCciM4uWlyGivpTf00b8TIvy6BEpBAZVCA9J5w"));
        credentials.setPublicKeyCose(Base64.getDecoder().decode("pQECAyYgASFYIEayvcdalRrrCPEidpoYbZdHmNsDeIyYBoVJ6HnwmUq4IlggV4V9TNhyHSGQxDTr4+TUWWP60edcpQlybrwOlIrxacU="));
        credentials.setCount(1L);
        credentialsRepository.save(credentials);

        AssertionRequest assertionRequest = mapper.readValue("{\"assertionId\":\"bWnC7+6A/fUcwjl048iPOQ==\",\"publicKeyCredentialRequestOptions\":{\"challenge\":\"UeBYkJu4cvNqx6FFi4qSIL8KIDox0pqyMS9W6bAbTH8\",\"rpId\":\"localhost\",\"allowCredentials\":[{\"type\":\"public-key\",\"id\":\"ARgxyHfw5N83gRMl2M7vHhqkQmtHwDJ8QCciM4uWlyGivpTf00b8TIvy6BEpBAZVCA9J5w\"}],\"userVerification\":\"preferred\",\"extensions\":{}}}", AssertionRequest.class);
        AssertionStartResponse startResponse = new AssertionStartResponse("obumqZhCl7CBKxpRjyMePA==", assertionRequest);
        when(assertionOperation.get(anyString())).thenReturn(startResponse);

        AssertionStartRequest request = new AssertionStartRequest();
        request.setUsername("junit");

        client
                .post()
                .uri("/assertion/finish")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just("{\n" +
                        "  \"assertionId\": \"bWnC7+6A/fUcwjl048iPOQ==\",\n" +
                        "  \"credential\": {\n" +
                        "    \"type\": \"public-key\",\n" +
                        "    \"id\": \"ARgxyHfw5N83gRMl2M7vHhqkQmtHwDJ8QCciM4uWlyGivpTf00b8TIvy6BEpBAZVCA9J5w\",\n" +
                        "    \"rawId\": \"ARgxyHfw5N83gRMl2M7vHhqkQmtHwDJ8QCciM4uWlyGivpTf00b8TIvy6BEpBAZVCA9J5w\",\n" +
                        "    \"response\": {\n" +
                        "      \"clientDataJSON\": \"eyJ0eXBlIjoid2ViYXV0aG4uZ2V0IiwiY2hhbGxlbmdlIjoiVWVCWWtKdTRjdk5xeDZGRmk0cVNJTDhLSURveDBwcXlNUzlXNmJBYlRIOCIsIm9yaWdpbiI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MCIsImNyb3NzT3JpZ2luIjpmYWxzZX0\",\n" +
                        "      \"authenticatorData\": \"SZYN5YgOjGh0NBcPZHZgW4_krrmihjLHmVzzuoMdl2MFYQFsow\",\n" +
                        "      \"signature\": \"MEUCIFnff70nAto5eJTwyVHYgoi_E3013MOnbUVHJWIfaWbWAiEA9tw1WfZjTl1LOx3JF4-HQVPDhvVNVpRMXmtR2BN3m9I\",\n" +
                        "      \"userHandle\": \"AAAAAAAAAAE\"\n" +
                        "    },\n" +
                        "    \"clientExtensionResults\": {}\n" +
                        "  }\n" +
                        "}\n"), String.class)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(s -> log.info(s.toString()))
                .jsonPath("$.name").isEqualTo("junit");
    }

    @Test
    public void testRegistrationStart() {

        RegistrationStartRequest request = new RegistrationStartRequest();
        request.setUsername("newjunit");
        client
                .post()
                .uri("/registration/start")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(request), RegistrationStartRequest.class)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(s -> log.info(s.toString()));
    }


    @Test
    public void testAddDeviceInvalidToken() {

        RegistrationStartRequest request = new RegistrationStartRequest();
        request.setRegistrationAddToken("token-123");
        client
                .post()
                .uri("/registration/start")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(request), RegistrationStartRequest.class)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .consumeWith(s -> log.info(s.toString()));
    }

    @Test
    public void testAddDeviceInvalidTokenNotFound() {

        RegistrationStartRequest request = new RegistrationStartRequest();
        request.setRegistrationAddToken(Base64.getEncoder().encodeToString("token-123".getBytes()));
        client
                .post()
                .uri("/registration/start")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(request), RegistrationStartRequest.class)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .consumeWith(s -> log.info(s.toString()));
    }

    @Test
    public void testAddDevice() {
        byte[] bytes = "token-123".getBytes();
        String registrationAddToken = Base64.getEncoder().encodeToString(bytes);

        JpaWebAuthnUser user = new JpaWebAuthnUser();
        user.setUsername("username-add");
        user.setAddToken(bytes);
        user.setRegistrationAddStart(LocalDateTime.now().minusMinutes(1));
        webAuthnUserRepository.save(user);

        RegistrationStartRequest request = new RegistrationStartRequest();
        request.setRegistrationAddToken(registrationAddToken);
        client
                .post()
                .uri("/registration/start")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(request), RegistrationStartRequest.class)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(s -> log.info(s.toString()))
                .jsonPath("$.status").isEqualTo("OK")
                .jsonPath("$.registrationId").exists()
                .jsonPath("$.publicKeyCredentialCreationOptions.rp.id").isEqualTo("localhost")
                .jsonPath("$.publicKeyCredentialCreationOptions.user.name").isEqualTo(user.getUsername())
                .jsonPath("$.publicKeyCredentialCreationOptions.user.id").exists();
    }

    @Test
    public void testRecoveryToken() {
        byte[] bytes = "token-123".getBytes();
        String token = Base64.getEncoder().encodeToString(bytes);

        JpaWebAuthnUser user = new JpaWebAuthnUser();
        user.setUsername("user-recovery");
        user.setRecoveryToken(bytes);
        webAuthnUserRepository.save(user);

        RegistrationStartRequest request = new RegistrationStartRequest();
        request.setRecoveryToken(token);

        client
                .post()
                .uri("/registration/start")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(request), RegistrationStartRequest.class)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(s -> log.info(s.toString()))
                .jsonPath("$.status").isEqualTo("OK")
                .jsonPath("$.registrationId").exists()
                .jsonPath("$.publicKeyCredentialCreationOptions.rp.id").isEqualTo("localhost")
                .jsonPath("$.publicKeyCredentialCreationOptions.user.name").isEqualTo(user.getUsername())
                .jsonPath("$.publicKeyCredentialCreationOptions.user.id").exists();
    }

    @Test
    public void testRecoveryTokenInvalid() {

        RegistrationStartRequest request = new RegistrationStartRequest();
        request.setRecoveryToken(Base64.getEncoder().encodeToString("token-321".getBytes()));
        client
                .post()
                .uri("/registration/start")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(request), RegistrationStartRequest.class)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .consumeWith(s -> log.info(s.toString()));
    }

    @Test
    @WithMockUser("user-existing")
    public void testRegisterCredentialsForExistingUser() {

        JpaWebAuthnUser user = new JpaWebAuthnUser();
        user.setUsername("user-existing");
        webAuthnUserRepository.save(user);

        JpaWebAuthnCredentials credentials = new JpaWebAuthnCredentials();
        credentials.setAppUserId(user.getId());
        credentials.setCredentialId(BytesUtil.longToBytes(123L));
        credentialsRepository.save(credentials);

        RegistrationStartRequest request = new RegistrationStartRequest();
        client.mutateWith((c, d, e) -> {
                    c.responseTimeout(Duration.ofDays(1));
                })
                .post()
                .uri("/registration/start")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(request), RegistrationStartRequest.class)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(s -> log.info(s.toString()));
    }

    @Test
    public void testRegisterCredentialsNoInput() {

        RegistrationStartRequest request = new RegistrationStartRequest();
        client
                .post()
                .uri("/registration/start")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(request), RegistrationStartRequest.class)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .consumeWith(s -> log.info(s.toString()));
    }

}
