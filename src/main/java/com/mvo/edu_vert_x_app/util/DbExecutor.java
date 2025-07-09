package com.mvo.edu_vert_x_app.util;

import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnection;

import java.util.function.Function;

public interface DbExecutor {
  <T> Future<T> execute(Function<SqlConnection, Future<T>> operation);
}
