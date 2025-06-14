package com.mvo.edu_vert_x_app.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
  public ApiException(String message) {
    super(message);
  }
}
