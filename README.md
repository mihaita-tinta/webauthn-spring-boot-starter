# webauthn-spring-boot-starter
![Build status](https://github.com/mihaita-tinta/spring-boot-starter-webauthn/actions/workflows/maven.yml/badge.svg?branch=main)
![Code coverage](.github/badges/jacoco.svg)
![Code coverage](.github/badges/branches.svg)

Simple spring boot starter based on [Yubico/java-webauthn-server](https://github.com/Yubico/java-webauthn-server)

You can checkout [this](https://github.com/mihaita-tinta/spring-boot-starter-webauthn-demo) repo to run a simple example.

Another example using a Flutter client is [here](https://github.com/mihaita-tinta/flutter-webauthn-demo)

<img src="https://github.com/mihaita-tinta/flutter-webauthn-demo/blob/main/register.gif" width="250"/>


The documentation is available [here](https://webauthn-spring-boot-starter.glitch.me/)

Add the dependency into your `pom.xml`
```
<dependency>
    <groupId>io.github.mihaita-tinta</groupId>
    <artifactId>webauthn-spring-boot-starter</artifactId>
    <version>0.7.0-RELEASE</version>
</dependency>
```
Customize different callbacks to detect when something happens

```java
@Configuration
@EnableWebSecurity
@EnableWebAuthn
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    AccountRepository accountRepository;

    @Override
    public void configure(WebSecurity web) {
        web
                .ignoring()
                .antMatchers(
                        "/",
                        "/register.html",
                        "/login.html",
                        "/new-device.html",
                        "/node_modules/web-authn-components/dist/**",
                        "/error"
                );
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .headers().frameOptions().sameOrigin()
                .and()
                .logout(customizer -> {
                    customizer.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
                    customizer.deleteCookies("JSESSIONID");
                })
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
                .apply(new WebAuthnConfigurer()
                        .userSupplier(() ->
                                Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                                        .map(authn -> userRepository.findByUsername(authn.getName())
                                                .orElseThrow() //here you can migrate users in the webauthn user repository
                                        )
                                        .orElse(null) // registering a new user account for unauthenticated requests

                        ));
    }
}

```

For a Spring WebFlux application you can activate the WebAuthn filter with:

```java
@SpringBootApplication
@EnableWebFlux
@EnableWebFluxSecurity
@EnableWebAuthn
public class SpringWebFluxTestConfig {
    
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, Supplier<WebAuthnWebFilter> webAuthnWebFilterSupplier) {

        http
                .authorizeExchange()
                .anyExchange()
                .authenticated()
                .and()
                .cors()
                .and()
                .addFilterAfter(webAuthnWebFilterSupplier.get()
                                .withAuthenticationSuccessHandler((finish, authentication) ->
                                        Map.of("name", authentication.getName()))
                                .withUser(ReactiveSecurityContextHolder.getContext()
                                        .flatMap(sc -> {
                                            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) sc.getAuthentication();
                                            if (token == null)
                                                return Mono.empty();

                                            Object principal = token.getPrincipal();
                                            if (principal instanceof DefaultWebAuthnUser) {
                                                return Mono.just((DefaultWebAuthnUser) principal);
                                            } else {
                                                DefaultWebAuthnUser u = new DefaultWebAuthnUser();
                                                u.setUsername(token.getName());

                                                return Mono.just(userRepository.findByUsername(u.getUsername()).orElseGet(() ->
                                                        userRepository.save(u)
                                                ));
                                            }
                                        }))
                        , SecurityWebFiltersOrder.AUTHENTICATION)
                .csrf()
                .disable()
        ;

        return http.build();
    }
}
```
You can change the response when the request was successfuly authenticated. In the example below we are returning the username, but a different authentication token could be used.
```
webAuthnWebFilterSupplier
//...
 .withAuthenticationSuccessHandler((finish, authentication) ->
                                        Map.of("name", authentication.getName()))
```

There are different properties you can change depending on your needs.
application.yaml

```yaml
webauthn:
  relying-party-id: localhost
  relying-party-name: Example Application
  relying-party-icon: http://localhost:8080/assets/logo.png
  relying-party-origins: http://localhost:8080
  registrationNewUsers:
     enabled: true
  username-required: true
  endpoints:
    registrationStartPath: /api/registration/start
    registrationAddPath: /api/registration/add
    registrationFinishPath: /api/registration/finish
    assertionStartPath: /api/assertion/start
    assertionFinishPath: /api/assertion/finish
  preferred-pubkey-params:
      -
        alg: EdDSA
        type: PUBLIC_KEY
      -
        alg: ES256
        type: PUBLIC_KEY
      -
        alg: RS256
        type: PUBLIC_KEY
      -
        alg: RS1
        type: PUBLIC_KEY
spring:
  resources:
    static-locations: classpath:/META-INF/resources/webauthn
  jackson:
    default-property-inclusion: non_absent
    serialization:
      FAIL_ON_EMPTY_BEANS: false
  h2:
    console:
      enabled: true
```
