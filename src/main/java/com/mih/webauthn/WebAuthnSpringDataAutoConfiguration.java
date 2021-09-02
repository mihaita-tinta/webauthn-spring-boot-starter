package com.mih.webauthn;


import com.mih.webauthn.domain.*;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.Repository;

@Configuration
@ConditionalOnClass(Repository.class)
@EnableJpaRepositories
@EntityScan("com.mih.webauthn.domain")
@AutoConfigureAfter(HibernateJpaAutoConfiguration.class)
public class WebAuthnSpringDataAutoConfiguration {

    @Bean
    public WebAuthnUserRepository webAuthnUserJpaRepository(WebAuthnUserSpringDataRepository repo) {
        return new SpringDataWebAuthnUserRepositoryAdapter(repo);
    }

    @Bean
    public WebAuthnCredentialsRepository webAuthnCredentialsJpaRepository(WebAuthnCredentialsSpringDataRepository repo) {
        return new SpringDataWebAuthnCredentialsRepositoryAdapter(repo);
    }
}
