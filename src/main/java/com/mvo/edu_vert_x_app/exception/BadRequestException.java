package com.mvo.edu_vert_x_app.exception;

public class BadRequestException extends ApiException {
  public BadRequestException(String message) {
    super(message);
  }
}
