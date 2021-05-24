package com.mih.webauthn.config;

import com.mih.webauthn.domain.WebAuthnUser;

public interface SuccessHandler {

    void onUser(WebAuthnUser user);
}
