package com.mvo.edu_vert_x_app.util;

import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlConnection;

import java.util.function.Function;

public class TxExecutor implements DbExecutor {
  private final Pool client;

  public TxExecutor(Pool client) {
    this.client = client;
  }

  @Override
  public <T> Future<T> execute(Function<SqlConnection, Future<T>> operation) {
    return client.withTransaction(operation);
  }
}
