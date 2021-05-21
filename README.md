# spring-boot-starter-webauthn
Simple spring boot starter based on Yubico/java-webauthn-server

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
                .apply(
                        new WebauthnConfigurer(appUserRepository, credentialRepository,
                                                credentialService, relyingParty, mapper)
                .successHandler(u -> {

                    AppUserDetail userDetail = new AppUserDetail(u,
                            new SimpleGrantedAuthority("USER"));
                    AppUserAuthentication auth = new AppUserAuthentication(userDetail);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }));
    }
```
