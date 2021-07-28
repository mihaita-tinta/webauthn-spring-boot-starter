package com.mih.webauthn;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mih.webauthn.config.WebAuthnOperation;
import com.mih.webauthn.domain.*;
import com.mih.webauthn.dto.AssertionStartResponse;
import com.mih.webauthn.dto.RegistrationStartResponse;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
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
public class AssertionFinishTest {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    WebAuthnUserRepository webAuthnUserRepository;
    @Autowired
    WebAuthnCredentialsRepository credentialsRepository;

    @MockBean
    WebAuthnOperation assertionOperation;

    @Test
    public void testFinish() throws Exception {

        WebAuthnDefaultUser user = new WebAuthnDefaultUser();
        user.setUsername("junit");
        user.setId(1L);
        user = webAuthnUserRepository.save(user);

        WebAuthnDefaultCredentials credentials = new WebAuthnDefaultCredentials();
        credentials.setAppUserId(user.getId());
        credentials.setCredentialId(Base64.getUrlDecoder().decode("AandphtQ5RDYYS3CkUfOLhBa2AYBVYx-oi3sd-4FdendRLYRa7lK-JEBcg7OtDTwZuh0fw"));
        credentials.setPublicKeyCose(Base64.getUrlDecoder().decode("pQECAyYgASFYILHVnnRS_5WOwlCpML-7Nd-DQwvrbogW4AWr_gU46rY0IlggTj9JCr-AVRe73qUOrENgV71N1ffrKOoBTVOTPBrYKR0"));
        credentials.setCount(1L);
        credentialsRepository.save(credentials);

        AssertionRequest assertionRequest = mapper.readValue("{\"assertionId\":\"mu3Btl2cyN/kOJYuJm3bvw==\",\"publicKeyCredentialRequestOptions\":{\"challenge\":\"UM0W1FmvQ0z2ijaGKxzobOU-NlWAaT9TW6rTIqLUXgk\",\"rpId\":\"localhost\",\"allowCredentials\":[{\"type\":\"public-key\",\"id\":\"AandphtQ5RDYYS3CkUfOLhBa2AYBVYx-oi3sd-4FdendRLYRa7lK-JEBcg7OtDTwZuh0fw\"}],\"userVerification\":\"preferred\",\"extensions\":{}}}", AssertionRequest.class);
        AssertionStartResponse startResponse = new AssertionStartResponse("BvrOasdbq3ZZTCJroVmMXw==", assertionRequest);
        when(assertionOperation.get(anyString())).thenReturn(startResponse);

        this.mockMvc.perform(
                post("/assertion/finish")
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "  \"assertionId\": \"mu3Btl2cyN/kOJYuJm3bvw==\",\n" +
                                "  \"credential\": {\n" +
                                "    \"type\": \"public-key\",\n" +
                                "    \"id\": \"AandphtQ5RDYYS3CkUfOLhBa2AYBVYx-oi3sd-4FdendRLYRa7lK-JEBcg7OtDTwZuh0fw\",\n" +
                                "    \"rawId\": \"AandphtQ5RDYYS3CkUfOLhBa2AYBVYx-oi3sd-4FdendRLYRa7lK-JEBcg7OtDTwZuh0fw\",\n" +
                                "    \"response\": {\n" +
                                "      \"clientDataJSON\": \"eyJ0eXBlIjoid2ViYXV0aG4uZ2V0IiwiY2hhbGxlbmdlIjoiVU0wVzFGbXZRMHoyaWphR0t4em9iT1UtTmxXQWFUOVRXNnJUSXFMVVhnayIsIm9yaWdpbiI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MCIsImNyb3NzT3JpZ2luIjpmYWxzZSwib3RoZXJfa2V5c19jYW5fYmVfYWRkZWRfaGVyZSI6ImRvIG5vdCBjb21wYXJlIGNsaWVudERhdGFKU09OIGFnYWluc3QgYSB0ZW1wbGF0ZS4gU2VlIGh0dHBzOi8vZ29vLmdsL3lhYlBleCJ9\",\n" +
                                "      \"authenticatorData\": \"SZYN5YgOjGh0NBcPZHZgW4_krrmihjLHmVzzuoMdl2MFYPllMA\",\n" +
                                "      \"signature\": \"MEUCIQDxmGYnzZIqkdx38TnS3zKHiO80215VbXeoj-wT_t5iXwIgZvh9d4tZwlw6cijbAF_mxqJ_1QwWsnc9-6QsJIRiuPY\",\n" +
                                "      \"userHandle\": \"AAAAAAAAAAI\"\n" +
                                "    },\n" +
                                "    \"clientExtensionResults\": {}\n" +
                                "  }\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("assertion-finish"));
    }

}
