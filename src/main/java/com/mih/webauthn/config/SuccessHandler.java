package com.mih.webauthn.config;

import com.mih.webauthn.domain.AppUser;

public interface SuccessHandler {

    void onUser(AppUser user);
}
