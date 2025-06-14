package com.mvo.edu_vert_x_app.exception;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.time.Instant;

public class GlobalErrorHandler implements Handler<RoutingContext> {
  @Override
  public void handle(RoutingContext context) {
    Throwable error = context.failure();
    int statusCode = determineStatusCode(error);
    String message = error.getMessage();
    context.response()
      .setStatusCode(statusCode)
      .putHeader("Content-Type", "application/json")
      .end(
        new JsonObject()
          .put("timestamp", Instant.now().toString())
          .put("status", statusCode)
          .put("error", error.getClass().getSimpleName())
          .put("message", message)
          .encode()
      );
  }

  private int determineStatusCode(Throwable error) {
    return switch (error) {
      case NotFoundEntityException exception -> 404;
      default -> 500;
    };
  }
}
