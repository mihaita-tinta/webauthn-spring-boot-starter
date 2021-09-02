package io.github.webauthn;

import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.COSEAlgorithmIdentifier;
import com.yubico.webauthn.data.PublicKeyCredentialParameters;
import com.yubico.webauthn.data.PublicKeyCredentialType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.validation.constraints.NotEmpty;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@ConfigurationProperties(prefix = "webauthn")
public class WebAuthnProperties {
    /**
     * In the context of the WebAuthn API, a relying party identifier is a valid domain string identifying the WebAuthn Relying Party on whose behalf a given registration or authentication ceremony is being performed.
     *
     * @see RelyingParty#getIdentity()
     */
    @NotEmpty
    private String relyingPartyId;

    @NotEmpty
    private String relyingPartyName;

    private URL relyingPartyIcon;

    private List<PublicKeyAlgorithm> preferredPubkeyParams;

    /**
     * The set of origins on which the public key credential may be exercised
     */
    @NotEmpty
    private Set<String> relyingPartyOrigins;

    private FilterPaths endpoints = new FilterPaths();

    public String getRelyingPartyId() {
        return this.relyingPartyId;
    }

    public void setRelyingPartyId(String relyingPartyId) {
        this.relyingPartyId = relyingPartyId;
    }

    public String getRelyingPartyName() {
        return this.relyingPartyName;
    }

    public void setRelyingPartyName(String relyingPartyName) {
        this.relyingPartyName = relyingPartyName;
    }

    public URL getRelyingPartyIcon() {
        return this.relyingPartyIcon;
    }

    public void setRelyingPartyIcon(URL relyingPartyIcon) {
        this.relyingPartyIcon = relyingPartyIcon;
    }

    public Set<String> getRelyingPartyOrigins() {
        return this.relyingPartyOrigins;
    }

    public void setRelyingPartyOrigins(Set<String> relyingPartyOrigins) {
        this.relyingPartyOrigins = relyingPartyOrigins;
    }

    public FilterPaths getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(FilterPaths endpoints) {
        this.endpoints = endpoints;
    }

    public List<PublicKeyCredentialParameters> getPreferredPubkeyParams() {
        if (preferredPubkeyParams == null)
            return null;
        return preferredPubkeyParams
                .stream()
                .map(alg -> PublicKeyCredentialParameters.builder()
                        .alg(alg.alg)
                        .type(alg.type)
                        .build())
                .collect(Collectors.toList());
    }

    public void setPreferredPubkeyParams(List<PublicKeyAlgorithm> preferredPubkeyParams) {
        this.preferredPubkeyParams = preferredPubkeyParams;
    }

    public static class FilterPaths {
        private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REGISTRATION_START_REQUEST_MATCHER = new AntPathRequestMatcher("/registration/start", "POST");
        private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REGISTRATION_ADD_REQUEST_MATCHER = new AntPathRequestMatcher("/registration/add", "GET");
        private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REGISTRATION_FINISH_REQUEST_MATCHER = new AntPathRequestMatcher("/registration/finish", "POST");
        private static final AntPathRequestMatcher DEFAULT_ANT_PATH_ASSERTION_START_REQUEST_MATCHER = new AntPathRequestMatcher("/assertion/start", "POST");
        private static final AntPathRequestMatcher DEFAULT_ANT_PATH_ASSERTION_FINISH_REQUEST_MATCHER = new AntPathRequestMatcher("/assertion/finish", "POST");


        private AntPathRequestMatcher registrationStartPath;
        private AntPathRequestMatcher registrationAddPath;
        private AntPathRequestMatcher registrationFinishPath;
        private AntPathRequestMatcher assertionStartPath;
        private AntPathRequestMatcher assertionFinishPath;

        public AntPathRequestMatcher getRegistrationStartPath() {
            return ofNullable(registrationStartPath)
                    .orElse(DEFAULT_ANT_PATH_REGISTRATION_START_REQUEST_MATCHER);
        }

        public void setRegistrationStartPath(String registrationStartPath) {
            this.registrationStartPath = new AntPathRequestMatcher(registrationStartPath, "POST");
        }

        public AntPathRequestMatcher getRegistrationAddPath() {
            return ofNullable(registrationAddPath)
                    .orElse(DEFAULT_ANT_PATH_REGISTRATION_ADD_REQUEST_MATCHER);
        }

        public void setRegistrationAddPath(String registrationAddPath) {
            this.registrationAddPath = new AntPathRequestMatcher(registrationAddPath, "GET");
        }

        public AntPathRequestMatcher getRegistrationFinishPath() {
            return ofNullable(registrationFinishPath)
                    .orElse(DEFAULT_ANT_PATH_REGISTRATION_FINISH_REQUEST_MATCHER);
        }

        public void setRegistrationFinishPath(String registrationFinishPath) {
            this.registrationFinishPath = new AntPathRequestMatcher(registrationFinishPath, "POST");
        }

        public AntPathRequestMatcher getAssertionStartPath() {
            return ofNullable(assertionStartPath)
                    .orElse(DEFAULT_ANT_PATH_ASSERTION_START_REQUEST_MATCHER);
        }

        public void setAssertionStartPath(String assertionStartPath) {
            this.assertionStartPath = new AntPathRequestMatcher(assertionStartPath, "POST");
        }

        public AntPathRequestMatcher getAssertionFinishPath() {
            return ofNullable(assertionFinishPath)
                    .orElse(DEFAULT_ANT_PATH_ASSERTION_FINISH_REQUEST_MATCHER);
        }

        public void setAssertionFinishPath(String assertionFinishPath) {
            this.assertionFinishPath = new AntPathRequestMatcher(assertionFinishPath, "POST");
        }
    }

    public static class PublicKeyAlgorithm {
        private COSEAlgorithmIdentifier alg;
        private PublicKeyCredentialType type;

        public COSEAlgorithmIdentifier getAlg() {
            return alg;
        }

        public void setAlg(COSEAlgorithmIdentifier alg) {
            this.alg = alg;
        }

        public PublicKeyCredentialType getType() {
            return type;
        }

        public void setType(PublicKeyCredentialType type) {
            this.type = type;
        }
    }

}
