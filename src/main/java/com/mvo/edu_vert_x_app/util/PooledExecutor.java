package com.mvo.edu_vert_x_app.util;

import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlConnection;

import java.util.function.Function;

public class PooledExecutor implements DbExecutor {
  private final Pool pool;

  public PooledExecutor(Pool pool) {
    this.pool = pool;
  }

  @Override
  public <T> Future<T> execute(Function<SqlConnection, Future<T>> operation) {
    return pool.withConnection(operation);
  }
}
