package io.github.webauthn.dto;

public class AssertionStartRequest {

  private String username;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @Override
  public String toString() {
    return "AssertionStartRequest{" +
            "username='" + username + '\'' +
            '}';
  }
}
