package com.mvo.edu_vert_x_app.config;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;

public class DbConfig {

  public Pool getPool(Vertx vertx, JsonObject properties) {
      PgConnectOptions connectOptions = new PgConnectOptions()
      .setPort(properties.getInteger("port"))
      .setHost(properties.getString("host"))
      .setDatabase(properties.getString("database"))
      .setUser(properties.getString("user"))
      .setPassword(properties.getString("password"));

    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    return Pool.pool(vertx, connectOptions, poolOptions);
  }
}
