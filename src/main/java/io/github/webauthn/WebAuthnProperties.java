package io.github.webauthn;

import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.COSEAlgorithmIdentifier;
import com.yubico.webauthn.data.PublicKeyCredentialParameters;
import com.yubico.webauthn.data.PublicKeyCredentialType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
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

    private boolean usernameRequired = true;

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

    public boolean isUsernameRequired() {
        return usernameRequired;
    }

    public void setUsernameRequired(boolean usernameRequired) {
        this.usernameRequired = usernameRequired;
    }

    public static class FilterPaths {

        private String registrationStartPath = "/registration/start";
        private String registrationAddPath = "/registration/add";
        private String registrationFinishPath = "/registration/finish";
        private String assertionStartPath = "/assertion/start";
        private String assertionFinishPath = "/assertion/finish";

        public AntPathRequestMatcher getRegistrationStartPath() {
            return new AntPathRequestMatcher(registrationStartPath, "POST");
        }
        public ServerWebExchangeMatcher getRegistrationStartPathWebFlux() {
            return ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, registrationStartPath);
        }

        public void setRegistrationStartPath(String registrationStartPath) {
            this.registrationStartPath = registrationStartPath;
        }

        public ServerWebExchangeMatcher getRegistrationAddPathWebFlux() {
            return ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, registrationAddPath);
        }
        public AntPathRequestMatcher getRegistrationAddPath() {
            return new AntPathRequestMatcher(registrationAddPath, "GET");
        }
        public void setRegistrationAddPath(String registrationAddPath) {
            this.registrationAddPath = registrationAddPath;
        }

        public AntPathRequestMatcher getRegistrationFinishPath() {
            return new AntPathRequestMatcher(registrationFinishPath, "POST");
        }

        public ServerWebExchangeMatcher getRegistrationFinishPathWebFlux() {
            return ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, registrationFinishPath);
        }

        public void setRegistrationFinishPath(String registrationFinishPath) {
            this.registrationFinishPath = registrationFinishPath;
        }

        public AntPathRequestMatcher getAssertionStartPath() {
            return new AntPathRequestMatcher(assertionStartPath, "POST");
        }

        public ServerWebExchangeMatcher getAssertionStartPathWebFlux() {
            return ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, assertionStartPath);
        }

        public void setAssertionStartPath(String assertionStartPath) {
            this.assertionStartPath = assertionStartPath;
        }

        public AntPathRequestMatcher getAssertionFinishPath() {
            return new AntPathRequestMatcher(assertionFinishPath, "POST");
        }

        public ServerWebExchangeMatcher getAssertionFinishPathWebFlux() {
            return ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, assertionFinishPath);
        }

        public void setAssertionFinishPath(String assertionFinishPath) {
            this.assertionFinishPath = assertionFinishPath;
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
