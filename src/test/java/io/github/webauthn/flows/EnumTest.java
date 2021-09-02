package io.github.webauthn.flows;

import com.yubico.webauthn.data.COSEAlgorithmIdentifier;
import com.yubico.webauthn.data.PublicKeyCredentialType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EnumTest {

    @Test
    public void test() {
        assertNotNull(Enum.valueOf(COSEAlgorithmIdentifier.class, "EdDSA"));
        assertNotNull(Enum.valueOf(PublicKeyCredentialType.class, "PUBLIC_KEY"));

    }
}
