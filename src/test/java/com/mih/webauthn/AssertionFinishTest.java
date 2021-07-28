package com.mih.webauthn;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mih.webauthn.config.WebAuthnOperation;
import com.mih.webauthn.domain.*;
import com.mih.webauthn.dto.AssertionStartResponse;
import com.yubico.webauthn.AssertionRequest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
@Disabled
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
        user.setId(2L);
        user = webAuthnUserRepository.save(user);

        WebAuthnCredentials credentials = new WebAuthnCredentials();
        credentials.setAppUserId(user.getId());
        credentials.setCredentialId(Base64.getDecoder().decode("AfxD0PTsuAt62V23kGMAmSRsM8YptGeY5ocZI4S3YL3mPtoN9Nd89d8zUrmttX99N8FaEw=="));
        credentials.setPublicKeyCose(Base64.getDecoder().decode("pQECAyYgASFYIDiPU3nyNEtXJOJRsmwPVGuTfBNuBeJy0rhv0UYfX6UoIlggXu6oP8AfYroXWtOodZ7OUnuNhnrt+8QU5quJcPpV8WE="));
        credentials.setCount(1L);
        credentialsRepository.save(credentials);

        AssertionRequest assertionRequest = mapper.readValue("{\"assertionId\":\"obumqZhCl7CBKxpRjyMePA==\",\"publicKeyCredentialRequestOptions\":{\"challenge\":\"aUmj9KF5vCVdvNlxir6wrDkz1IpUAxrwabtXUXWm6BU\",\"rpId\":\"localhost\",\"allowCredentials\":[{\"type\":\"public-key\",\"id\":\"AfxD0PTsuAt62V23kGMAmSRsM8YptGeY5ocZI4S3YL3mPtoN9Nd89d8zUrmttX99N8FaEw\"}],\"userVerification\":\"preferred\",\"extensions\":{}}}", AssertionRequest.class);
//        AssertionRequest assertionRequest = mapper.readValue("{\"assertionId\":\"mu3Btl2cyN/kOJYuJm3bvw==\",\"publicKeyCredentialRequestOptions\":{\"challenge\":\"UM0W1FmvQ0z2ijaGKxzobOU-NlWAaT9TW6rTIqLUXgk\",\"rpId\":\"localhost\",\"allowCredentials\":[{\"type\":\"public-key\",\"id\":\"AandphtQ5RDYYS3CkUfOLhBa2AYBVYx-oi3sd-4FdendRLYRa7lK-JEBcg7OtDTwZuh0fw\"}],\"userVerification\":\"preferred\",\"extensions\":{}}}", AssertionRequest.class);
        AssertionStartResponse startResponse = new AssertionStartResponse("obumqZhCl7CBKxpRjyMePA==", assertionRequest);
//        AssertionStartResponse startResponse = new AssertionStartResponse("BvrOasdbq3ZZTCJroVmMXw==", assertionRequest);
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
