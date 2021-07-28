package com.mih.webauthn.domain;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mih.webauthn.BytesUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
public class SpringDataIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(SpringDataIntegrationTest.class);
    @Autowired
    WebAuthnCredentialsRepository credentialsRepository;
    @Autowired
    WebAuthnUserRepository userRepository;

    @Autowired
    List<CrudRepository> repos;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void test() {

        repos.size();

        WebAuthnCredentials credentials = new WebAuthnCredentials();
        credentials.setAppUserId(100L);
        credentials.setCredentialId(Base64.getUrlDecoder().decode("AandphtQ5RDYYS3CkUfOLhBa2AYBVYx-oi3sd-4FdendRLYRa7lK-JEBcg7OtDTwZuh0fw"));
        credentials.setPublicKeyCose(Base64.getUrlDecoder().decode("pQECAyYgASFYILHVnnRS_5WOwlCpML-7Nd-DQwvrbogW4AWr_gU46rY0IlggTj9JCr-AVRe73qUOrENgV71N1ffrKOoBTVOTPBrYKR0"));
        credentials.setCount(1L);
        credentialsRepository.save(credentials);

        List<WebAuthnCredentials> find = credentialsRepository.findAllByAppUserId(100L);

        log.info("test - credentials: {}", find);

        assertEquals(1, find.size());


    }


    @Test
    public void testStartSpringData() throws Exception {

        WebAuthnUser user = new WebAuthnUser();
        user.setUsername("junit");
        userRepository.save(user);

        WebAuthnCredentials credentials = new WebAuthnCredentials();
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
}
