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
        classes = WebAuthnPropertiesTest.TestConfig.class,
        properties = {
                "webauthn.relyingPartyId=localhost",
                "webauthn.relyingPartyName=localhost",
                "webauthn.relyingPartyOrigins=http://localhost:8080",
        })
class WebAuthnPropertiesTest {

    @Autowired
    WebAuthnProperties props;

    @Test
    public void test() {
        assertEquals("localhost", props.getRelyingPartyId());
        assertEquals("localhost", props.getRelyingPartyName());
        assertEquals(Arrays.asList("http://localhost:8080").stream().collect(Collectors.toSet()),
                props.getRelyingPartyOrigins());
    }

    @Configuration
    @EnableConfigurationProperties(WebAuthnProperties.class)
    public static class TestConfig {

    }

}
