# webauthn-spring-boot-starter
![Build status](https://github.com/mihaita-tinta/spring-boot-starter-webauthn/actions/workflows/maven.yml/badge.svg?branch=main)
![Code coverage](.github/badges/jacoco.svg)
![Code coverage](.github/badges/branches.svg)

Simple spring boot starter based on Yubico/java-webauthn-server
You can checkout [this](https://github.com/mihaita-tinta/spring-boot-starter-webauthn-demo) repo to run a simple example

Add the dependency into your `pom.xml`
```
<dependency>
    <groupId>io.github.mihaita-tinta</groupId>
    <artifactId>webauthn-spring-boot-starter</artifactId>
    <version>0.0.10-SNAPSHOT</version>
</dependency>
```
Customize different callbacks to detect when something happens

```java

@Override
protected void configure(HttpSecurity http) throws Exception {
        http.csrf(customizer -> customizer.disable())
        .logout(customizer -> {
        customizer.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
        customizer.deleteCookies("JSESSIONID");
        })
        .authorizeRequests()
        .anyRequest()
        .authenticated()
        .and()
        .apply(new WebauthnConfigurer()
                .userSupplier(() -> {
                        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
                        return userRepository.findByUsername(token.getName())
                                         .orElseThrow();
                })
                .defaultLoginSuccessHandler((user, credentials) -> log.info("login - user: {} with credentials: {}", user, credentials))
                .registerSuccessHandler(user -> log.info("registerSuccessHandler - user: {}", user))
                );
        }
```

There are different properties you can change depending on your needs.
application.yaml

```yaml
webauthn:
  relying-party-id: localhost
  relying-party-name: Example Application
  relying-party-icon: http://localhost:8100/assets/logo.png
  relying-party-origins: http://localhost:4200
  endpoints:
    registrationStartPath: /my-registration-start
    registrationFinishPath: /my-registration-finish
    registrationAddPath: /my-registration-add
    assertionStartPath: /my-login-start
    assertionFinishPath: /my-login-finish
  preferred-pubkey-params:
      -
        alg: EdDSA
        type: PUBLIC_KEY
      -
        alg:ES256
        type: PUBLIC_KEY
      -
        alg: RS256
        type: PUBLIC_KEY
      -
        alg: RS1
        type: PUBLIC_KEY
```
