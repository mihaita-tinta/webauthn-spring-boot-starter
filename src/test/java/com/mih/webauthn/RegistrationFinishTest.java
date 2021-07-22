package com.mih.webauthn;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mih.webauthn.config.WebAuthnOperation;
import com.mih.webauthn.domain.WebAuthnCredentials;
import com.mih.webauthn.domain.WebAuthnCredentialsRepository;
import com.mih.webauthn.domain.WebAuthnUser;
import com.mih.webauthn.domain.WebAuthnUserRepository;
import com.mih.webauthn.dto.RegistrationStartResponse;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
public class RegistrationFinishTest {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    WebAuthnOperation registrationOperation;

    @MockBean
    WebAuthnCredentialsRepository credentialsRepository;

    @Autowired
    WebAuthnUserRepository webAuthnUserRepository;

    @Autowired
    RelyingParty relyingParty;

    @Test
    public void testNewUserFinish() throws Exception {

        WebAuthnUser user = new WebAuthnUser();
        user.setUsername("junit");
        webAuthnUserRepository.save(user);

        WebAuthnCredentials credentials = new WebAuthnCredentials();
        credentials.setAppUserId(user.getId());
        credentials.setCredentialId(BytesUtil.longToBytes(123L));
        credentialsRepository.save(credentials);

        PublicKeyCredentialCreationOptions credentialCreationOptions = mapper.readValue(
                "{\n" +
                        "    \"rp\": {\n" +
                        "      \"name\": \"Example Application\",\n" +
                        "      \"id\": \"localhost\",\n" +
                        "      \"icon\": \"http://localhost:8100/assets/logo.png\"\n" +
                        "    },\n" +
                        "    \"user\": {\n" +
                        "      \"name\": \"junit\",\n" +
                        "      \"displayName\": \"junit\",\n" +
                        "      \"id\": \"AAAAAAAAAAI\"\n" +
                        "    },\n" +
                        "    \"challenge\": \"Gkhjw8szWuGe2BFJ0Kmx1rt5az-lfyTs3Dy5eXKV-Bc\",\n" +
                        "    \"pubKeyCredParams\": [\n" +
                        "      {\n" +
                        "        \"alg\": -7,\n" +
                        "        \"type\": \"public-key\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"alg\": -8,\n" +
                        "        \"type\": \"public-key\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"alg\": -257,\n" +
                        "        \"type\": \"public-key\"\n" +
                        "      }\n" +
                        "    ],\n" +
                        "    \"excludeCredentials\": [],\n" +
                        "    \"attestation\": \"none\",\n" +
                        "    \"extensions\": {}\n" +
                        "  }", PublicKeyCredentialCreationOptions.class);
        RegistrationStartResponse startResponse = new RegistrationStartResponse(RegistrationStartResponse.Mode.NEW,
                "BvrOasdbq3ZZTCJroVmMXw==", credentialCreationOptions);
        when(registrationOperation.get(anyString())).thenReturn(startResponse);


        this.mockMvc.perform(
                post("/registration/finish")
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "  \"registrationId\": \"BvrOasdbq3ZZTCJroVmMXw==\",\n" +
                                "  \"credential\": {\n" +
                                "    \"type\": \"public-key\",\n" +
                                "    \"id\": \"AandphtQ5RDYYS3CkUfOLhBa2AYBVYx-oi3sd-4FdendRLYRa7lK-JEBcg7OtDTwZuh0fw\",\n" +
                                "    \"rawId\": \"AandphtQ5RDYYS3CkUfOLhBa2AYBVYx-oi3sd-4FdendRLYRa7lK-JEBcg7OtDTwZuh0fw\",\n" +
                                "    \"response\": {\n" +
                                "      \"clientDataJSON\": \"eyJ0eXBlIjoid2ViYXV0aG4uY3JlYXRlIiwiY2hhbGxlbmdlIjoiR2toanc4c3pXdUdlMkJGSjBLbXgxcnQ1YXotbGZ5VHMzRHk1ZVhLVi1CYyIsIm9yaWdpbiI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MCIsImNyb3NzT3JpZ2luIjpmYWxzZX0\",\n" +
                                "      \"attestationObject\": \"o2NmbXRkbm9uZWdhdHRTdG10oGhhdXRoRGF0YVi4SZYN5YgOjGh0NBcPZHZgW4_krrmihjLHmVzzuoMdl2NFYPld3a3OAAI1vMYKZIsLJfHwVQMANAGp3aYbUOUQ2GEtwpFHzi4QWtgGAVWMfqIt7HfuBXXp3US2EWu5SviRAXIOzrQ08GbodH-lAQIDJiABIVggsdWedFL_lY7CUKkwv7s134NDC-tuiBbgBav-BTjqtjQiWCBOP0kKv4BVF7vepQ6sQ2BXvU3V9-so6gFNU5M8GtgpHQ\"\n" +
                                "    },\n" +
                                "    \"clientExtensionResults\": {}\n" +
                                "  }\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(cookie().exists("JSESSIONID"))
                .andDo(document("registration-finish-new-user"));
    }

}
