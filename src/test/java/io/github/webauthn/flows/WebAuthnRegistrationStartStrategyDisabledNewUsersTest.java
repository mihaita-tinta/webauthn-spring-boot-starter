package io.github.webauthn.flows;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.webauthn.JsonConfig;
import io.github.webauthn.domain.WebAuthnCredentialsRepository;
import io.github.webauthn.domain.WebAuthnUserRepository;
import io.github.webauthn.dto.RegistrationStartRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
public class WebAuthnRegistrationStartStrategyDisabledNewUsersTest {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    WebAuthnUserRepository webAuthnUserRepository;
    @MockBean
    WebAuthnCredentialsRepository credentialsRepository;

    @Test

    public void testNewUserDisabled() throws Exception {

        RegistrationStartRequest request = new RegistrationStartRequest();
        mockMvc.perform(
                        post("/registration/start")
                                .accept(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("USER_REGISTRATION_DISABLED"))
                .andDo(document("registration-start-new-user-disabled"));

    }
}
