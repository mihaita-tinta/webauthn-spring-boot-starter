package io.github.webauthn.flows;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.webauthn.JsonConfig;
import io.github.webauthn.domain.WebAuthnUserRepository;
import io.github.webauthn.dto.RegistrationStartRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = {SpringMvcTestConfig.class, JsonConfig.class},
        properties = {
                "webauthn.relyingPartyId=localhost",
                "webauthn.relyingPartyName=localhost",
                "webauthn.relyingPartyOrigins=http://localhost:8080"
        })
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ActiveProfiles("custom-algorithms")
public class WebAuthnRegistrationStartStrategyCustomAlgorithmsTest {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    WebAuthnUserRepository webAuthnUserRepository;

    @Test
    public void testCustomAlgorithms() throws Exception {

        RegistrationStartRequest request = new RegistrationStartRequest();
        request.setUsername("newjunit");
        this.mockMvc.perform(
                        post("/registration/start")
                                .accept(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicKeyCredentialCreationOptions.pubKeyCredParams.length()").value(1))
                .andExpect(jsonPath("$.publicKeyCredentialCreationOptions.pubKeyCredParams.[0].alg").value(-8))
                .andExpect(jsonPath("$.publicKeyCredentialCreationOptions.pubKeyCredParams.[0].type").value("public-key"))
                .andDo(document("registration-start-custom-algorithms"));
    }

}
