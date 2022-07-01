package io.github.webauthn.flows;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.webauthn.data.ByteArray;
import io.github.webauthn.BytesUtil;
import io.github.webauthn.JsonConfig;
import io.github.webauthn.domain.*;
import io.github.webauthn.dto.RegistrationStartRequest;
import io.github.webauthn.jpa.JpaWebAuthnCredentials;
import io.github.webauthn.jpa.JpaWebAuthnUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Base64;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {SpringMvcTestConfig.class, JsonConfig.class},
        properties = {
                "webauthn.relyingPartyId=localhost",
                "webauthn.relyingPartyName=localhost",
                "webauthn.relyingPartyOrigins=http://localhost:8080"
        })
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
public class WebAuthnRegistrationStartStrategyTest {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    WebAuthnUserRepository webAuthnUserRepository;
    @MockBean
    WebAuthnCredentialsRepository credentialsRepository;

    @Test
    public void testNewUser() throws Exception {

        RegistrationStartRequest request = new RegistrationStartRequest();
        request.setUsername("newjunit");
        this.mockMvc.perform(
                        post("/registration/start")
                                .accept(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("registration-start-new-user"));
    }

    @Test
    public void testNewUserFailsWhenUsernameIsRequired() throws Exception {

        RegistrationStartRequest request = new RegistrationStartRequest();
        request.setFirstName("Gica");
        request.setLastName("Hagi");
        this.mockMvc.perform(
                        post("/registration/start")
                                .accept(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(document("registration-start-new-user-fails"));
    }

    @Test
    public void testAddDeviceInvalidToken() throws Exception {

        RegistrationStartRequest request = new RegistrationStartRequest();
        request.setRegistrationAddToken("token-123");
        this.mockMvc.perform(
                        post("/registration/start")
                                .accept(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(document("registration-start-add-device-invalid-token"));
    }

    @Test
    public void testAddDeviceInvalidTokenNotFound() throws Exception {

        RegistrationStartRequest request = new RegistrationStartRequest();
        request.setRegistrationAddToken(Base64.getEncoder().encodeToString("token-123".getBytes()));
        this.mockMvc.perform(
                        post("/registration/start")
                                .accept(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(document("registration-start-add-device-not-found-token"));
    }

    @Test
    public void testAddDevice() throws Exception {
        byte[] bytes = "token-123".getBytes();
        String registrationAddToken = Base64.getEncoder().encodeToString(bytes);

        JpaWebAuthnUser user = new JpaWebAuthnUser();
        user.setUsername("junit");
        user.setAddToken(bytes);
        user.setRegistrationAddStart(LocalDateTime.now().minusMinutes(1));
        webAuthnUserRepository.save(user);

        RegistrationStartRequest request = new RegistrationStartRequest();
        request.setRegistrationAddToken(registrationAddToken);
        this.mockMvc.perform(
                        post("/registration/start")
                                .accept(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.registrationId").exists())
                .andExpect(jsonPath("$.publicKeyCredentialCreationOptions.rp.id").value("localhost"))
                .andExpect(jsonPath("$.publicKeyCredentialCreationOptions.user.name").value(user.getUsername()))
                .andExpect(jsonPath("$.publicKeyCredentialCreationOptions.user.id").exists())
                .andDo(document("registration-start-add-device"));
    }

    @Test
    public void testRecoveryToken() throws Exception {
        byte[] bytes = "token-123".getBytes();
        String token = Base64.getEncoder().encodeToString(bytes);

        JpaWebAuthnUser user = new JpaWebAuthnUser();
        user.setUsername("junit");
        user.setRecoveryToken(bytes);
        webAuthnUserRepository.save(user);

        RegistrationStartRequest request = new RegistrationStartRequest();
        request.setRecoveryToken(token);
        this.mockMvc.perform(
                        post("/registration/start")
                                .accept(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.registrationId").exists())
                .andExpect(jsonPath("$.publicKeyCredentialCreationOptions.rp.id").value("localhost"))
                .andExpect(jsonPath("$.publicKeyCredentialCreationOptions.user.name").value(user.getUsername()))
                .andExpect(jsonPath("$.publicKeyCredentialCreationOptions.user.id").value(new ByteArray(BytesUtil.longToBytes(user.getId())).getBase64Url()))
                .andDo(document("registration-start-recovery"));
    }

    @Test
    public void testRecoveryTokenInvalid() throws Exception {

        RegistrationStartRequest request = new RegistrationStartRequest();
        request.setRecoveryToken(Base64.getEncoder().encodeToString("token-321".getBytes()));
        this.mockMvc.perform(
                        post("/registration/start")
                                .accept(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andDo(document("registration-start-recovery-invalid"));
    }

    @Test
    @WithMockUser("junit")
    public void testRegisterCredentialsForExistingUser() throws Exception {

        JpaWebAuthnUser user = new JpaWebAuthnUser();
        user.setUsername("junit");
        webAuthnUserRepository.save(user);

        JpaWebAuthnCredentials credentials = new JpaWebAuthnCredentials();
        credentials.setAppUserId(user.getId());
        credentials.setCredentialId(BytesUtil.longToBytes(123L));
        credentialsRepository.save(credentials);

        RegistrationStartRequest request = new RegistrationStartRequest();
        this.mockMvc.perform(
                        post("/registration/start")
                                .accept(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andDo(document("registration-start-existing-user"));
    }
}
