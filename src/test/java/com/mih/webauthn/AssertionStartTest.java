package com.mih.webauthn;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mih.webauthn.domain.*;
import com.mih.webauthn.dto.RegistrationStartRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = {
                "webauthn.relyingPartyId=localhost",
                "webauthn.relyingPartyName=localhost",
                "webauthn.relyingPartyOrigins=http://localhost:8080"
        })
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
public class AssertionStartTest {

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

        WebAuthnDefaultUser user = new WebAuthnDefaultUser();
        user.setUsername("junit");
        webAuthnUserRepository.save(user);

        WebAuthnDefaultCredentials credentials = new WebAuthnDefaultCredentials();
        credentials.setAppUserId(user.getId());
        credentials.setCredentialId(BytesUtil.longToBytes(123L));
        credentialsRepository.save(credentials);


        this.mockMvc.perform(
                post("/assertion/start")
                        .accept(MediaType.APPLICATION_JSON)
                        .content("junit")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("assertion-start"));
    }

    @Test
    public void testStartUserDoesntExist() throws Exception {

        this.mockMvc.perform(
                post("/assertion/start")
                        .accept(MediaType.APPLICATION_JSON)
                        .content("notexistingusername")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(document("assertion-start-user-not-found"));
    }

}
