# spring-boot-starter-webauthn
Simple spring boot starter based on Yubico/java-webauthn-server
You can checkout [this](https://github.com/mihaita-tinta/spring-boot-starter-webauthn-demo) repo to run a simple example

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
        .exceptionHandling(customizer -> customizer
        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .apply(new WebauthnConfigurer());
    }
```
