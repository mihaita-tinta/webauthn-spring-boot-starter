package com.mih.webauthn.flows;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mih.webauthn.BytesUtil;
import com.mih.webauthn.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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

        WebAuthnUser user = new WebAuthnUser();
        user.setUsername("junit");
        webAuthnUserRepository.save(user);

        WebAuthnCredentials credentials = new WebAuthnCredentials();
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
