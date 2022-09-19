package io.github.webauthn.flows;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.webauthn.data.ByteArray;
import io.github.webauthn.BytesUtil;
import io.github.webauthn.domain.DefaultWebAuthnCredentials;
import io.github.webauthn.domain.DefaultWebAuthnUser;
import io.github.webauthn.domain.WebAuthnCredentialsRepository;
import io.github.webauthn.domain.WebAuthnUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = {
                "webauthn.relyingPartyId=localhost",
                "webauthn.relyingPartyName=localhost",
                "webauthn.relyingPartyOrigins=http://localhost:8080"
        })
@AutoConfigureMockMvc
@AutoConfigureRestDocs
public class WebAuthnAssertionStartStrategyTest {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    WebAuthnUserRepository webAuthnUserRepository;
    @Autowired
    WebAuthnCredentialsRepository credentialsRepository;

    @Test
    public void testStart() throws Exception {

        DefaultWebAuthnUser user = new DefaultWebAuthnUser();
        user.setUsername("junit");
        webAuthnUserRepository.save(user);

        DefaultWebAuthnCredentials credentials = new DefaultWebAuthnCredentials();
        credentials.setAppUserId(user.getId());
        credentials.setCredentialId(BytesUtil.longToBytes(123L));
        credentialsRepository.save(credentials);


        this.mockMvc.perform(
                        post("/assertion/start")
                                .accept(MediaType.APPLICATION_JSON)
                                .content("{ \"username\": \"junit\"}")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("assertion-start"));
    }

    @Test
    public void testStartWithUserId() throws Exception {

        DefaultWebAuthnUser user = new DefaultWebAuthnUser();
        user.setUsername("junitUserId");
        webAuthnUserRepository.save(user);

        DefaultWebAuthnCredentials credentials = new DefaultWebAuthnCredentials();
        credentials.setAppUserId(user.getId());
        credentials.setCredentialId(BytesUtil.longToBytes(123L));
        credentialsRepository.save(credentials);


        this.mockMvc.perform(
                        post("/assertion/start")
                                .accept(MediaType.APPLICATION_JSON)
                                .content("{ \"userId\": \"" + new ByteArray(BytesUtil.longToBytes(user.getId())).getBase64Url() + "\"}")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("assertion-start"));
    }

    @Test
    public void testStartResidentKeys() throws Exception {

        this.mockMvc.perform(
                        post("/assertion/start")
                                .accept(MediaType.APPLICATION_JSON)
                                .content("{}")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("assertion-start-resident-keys"));
    }

    @Test
    public void testStartEmptyUsername() throws Exception {

        this.mockMvc.perform(
                        post("/assertion/start")
                                .accept(MediaType.APPLICATION_JSON)
                                .content("{ \"username\": \"\"}")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("assertion-start-empty-username"));
    }

    @Test
    public void testStartUserDoesntExist() throws Exception {

        this.mockMvc.perform(
                        post("/assertion/start")
                                .accept(MediaType.APPLICATION_JSON)
                                .content("{ \"username\": \"notexistingusername\"}")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(document("assertion-start-user-not-found"));
    }

    @Test
    public void testJsonParseException() throws Exception {

        this.mockMvc.perform(
                        post("/assertion/start")
                                .accept(MediaType.APPLICATION_JSON)
                                .content("adadsad")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(document("assertion-start-json-parse-exception"));
    }

}
