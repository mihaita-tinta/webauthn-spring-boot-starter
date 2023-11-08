package io.github.webauthn.dto;

import com.yubico.webauthn.data.ByteArray;

public class AssertionStartRequest {

  private String username;
  private ByteArray userId;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public ByteArray getUserId() {
    return userId;
  }

  public void setUserId(ByteArray userId) {
    this.userId = userId;
  }

  @Override
  public String toString() {
    return "AssertionStartRequest{" +
            "username='" + username + '\'' +
            "userId='" + userId + '\'' +
            '}';
  }
}
