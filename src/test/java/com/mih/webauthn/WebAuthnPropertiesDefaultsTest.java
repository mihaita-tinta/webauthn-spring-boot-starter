package com.mih.webauthn;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        properties = {
                "webauthn.relyingPartyId=localhost",
                "webauthn.relyingPartyName=localhost",
                "webauthn.relyingPartyOrigins=http://localhost:8080"
        })
class WebAuthnPropertiesDefaultsTest {

    @Autowired
    WebAuthnProperties props;

    @Test
    public void test() {
        assertEquals("/registration/start", props.getEndpoints().getRegistrationStartPath().getPattern());
    }

    @Configuration
    @EnableConfigurationProperties(WebAuthnProperties.class)
    public static class TestConfig {

    }

}
