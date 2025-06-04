package com.mvo.edu_vert_x_app.config;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.flywaydb.core.Flyway;

public class FlywayConfig {
  private final Vertx vertx;

  public FlywayConfig(Vertx vertx) {
    this.vertx = vertx;
  }

  public void configureFlyway(JsonObject properties) {

    Flyway flyway = Flyway.configure()
      .dataSource(properties.getString("url"),
        properties.getString("user"),
        properties.getString("password"))
      .locations(properties.getString("locations"))
      .load();

    flyway.migrate();
  }
}
