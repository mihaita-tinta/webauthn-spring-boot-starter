package com.mih.webauthn;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mih.webauthn.config.WebAuthnOperation;
import com.mih.webauthn.domain.*;
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
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
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
@Transactional
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
                        "      \"id\": \"AAAAAAAAAAE\"\n" +
                        "    },\n" +
                        "    \"challenge\": \"RvohhBba0q7HnIZIrKfaBnorlWg3zH6OGucCavQpzos\",\n" +
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
                "eMoXhE6Td45mfLES8FtoOw==", credentialCreationOptions);
        when(registrationOperation.get(anyString())).thenReturn(startResponse);


        this.mockMvc.perform(
                post("/registration/finish")
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "  \"registrationId\": \"eMoXhE6Td45mfLES8FtoOw==\",\n" +
                                "  \"credential\": {\n" +
                                "    \"type\": \"public-key\",\n" +
                                "    \"id\": \"AfxD0PTsuAt62V23kGMAmSRsM8YptGeY5ocZI4S3YL3mPtoN9Nd89d8zUrmttX99N8FaEw\",\n" +
                                "    \"rawId\": \"AfxD0PTsuAt62V23kGMAmSRsM8YptGeY5ocZI4S3YL3mPtoN9Nd89d8zUrmttX99N8FaEw\",\n" +
                                "    \"response\": {\n" +
                                "      \"clientDataJSON\": \"eyJ0eXBlIjoid2ViYXV0aG4uY3JlYXRlIiwiY2hhbGxlbmdlIjoiUnZvaGhCYmEwcTdIbklaSXJLZmFCbm9ybFdnM3pINk9HdWNDYXZRcHpvcyIsIm9yaWdpbiI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MCIsImNyb3NzT3JpZ2luIjpmYWxzZX0\",\n" +
                                "      \"attestationObject\": \"o2NmbXRkbm9uZWdhdHRTdG10oGhhdXRoRGF0YVi4SZYN5YgOjGh0NBcPZHZgW4_krrmihjLHmVzzuoMdl2NFYQE0Gq3OAAI1vMYKZIsLJfHwVQMANAH8Q9D07LgLetldt5BjAJkkbDPGKbRnmOaHGSOEt2C95j7aDfTXfPXfM1K5rbV_fTfBWhOlAQIDJiABIVggOI9TefI0S1ck4lGybA9Ua5N8E24F4nLSuG_RRh9fpSgiWCBe7qg_wB9iuhda06h1ns5Se42Geu37xBTmq4lw-lXxYQ\"\n" +
                                "    },\n" +
                                "    \"clientExtensionResults\": {}\n" +
                                "  }\n" +
                                "}\n")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("registration-finish-new-user"));
    }

}
