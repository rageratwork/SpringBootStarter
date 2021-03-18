package com.fourthwoods.starter.admin;

public class UsernameAlreadyExistsException extends Exception {
  public UsernameAlreadyExistsException() {
    super();
  }

  public UsernameAlreadyExistsException(String message) {
    super(message);
  }

  public UsernameAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }

  public UsernameAlreadyExistsException(Throwable cause) {
    super(cause);
  }

  protected UsernameAlreadyExistsException(String message, Throwable cause,
                      boolean enableSuppression,
                      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
