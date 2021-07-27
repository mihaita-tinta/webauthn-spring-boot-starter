package com.mih.webauthn.domain;


import com.mih.webauthn.TestSpringDataConfig;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.ActiveProfiles;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = TestSpringDataConfig.class)
@ActiveProfiles("spring-data")
public class SpringDataIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(SpringDataIntegrationTest.class);
    @Autowired
    MyCredentialsRepository credentialsRepository;

    @Autowired
    List<CrudRepository> repos;

    @Test
    public void test() {

        repos.size();

        MyCredentials credentials = new MyCredentials();
        credentials.setAppUserId(100L);
        credentials.setCredentialId(Base64.getUrlDecoder().decode("AandphtQ5RDYYS3CkUfOLhBa2AYBVYx-oi3sd-4FdendRLYRa7lK-JEBcg7OtDTwZuh0fw"));
        credentials.setPublicKeyCose(Base64.getUrlDecoder().decode("pQECAyYgASFYILHVnnRS_5WOwlCpML-7Nd-DQwvrbogW4AWr_gU46rY0IlggTj9JCr-AVRe73qUOrENgV71N1ffrKOoBTVOTPBrYKR0"));
        credentials.setCount(1L);
        ((CrudRepository)credentialsRepository).save(credentials);

        List<WebAuthnCredentials> find = credentialsRepository.findAllByAppUserId(100L);

        log.info("test - credentials: {}", find);

        assertEquals(1, find.size());


    }
}
