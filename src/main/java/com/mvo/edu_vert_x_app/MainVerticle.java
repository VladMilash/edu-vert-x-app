package com.mvo.edu_vert_x_app;

import com.mvo.edu_vert_x_app.dto.request.StudentTransientDTO;
import com.mvo.edu_vert_x_app.entity.Student;
import com.mvo.edu_vert_x_app.repository.StudentRepository;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.flywaydb.core.Flyway;

import java.util.concurrent.atomic.AtomicReference;

public class MainVerticle extends VerticleBase {
  private StudentRepository studentRepository;

  @Override
  public Future<?> start() {
    PgConnectOptions connectOptions = new PgConnectOptions()
      .setPort(5432)
      .setHost("localhost")
      .setDatabase("edu3")
      .setUser("postgres")
      .setPassword("4");

    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    Pool client = Pool.pool(vertx, connectOptions, poolOptions);

    configureFlyway();

    studentRepository = new StudentRepository();

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    //router.post("/api/v1/students").handler(context -> save(client, context));

    router.post("/api/v1/students").handler(context -> {
      JsonObject body = context.body().asJsonObject();
      var studentTransientDTO = new StudentTransientDTO(body.getString("name"),body.getString("email"));
      studentRepository.save(studentTransientDTO,client)
        .onSuccess(savedStudent -> {
          JsonObject responseBody = new JsonObject();
          responseBody.put("id", savedStudent.getId());
          responseBody.put("name", savedStudent.getName());
          responseBody.put("email", savedStudent.getEmail());

          context.response()
            .setStatusCode(201)
            .putHeader("Content-Type", "application/json")
            .end(responseBody.encode());
        }).onFailure(error -> context.response()
          .setStatusCode(500)
          .end("Error: " + error.getMessage()));
    });

    return vertx.createHttpServer()
      .requestHandler(router)
      .listen(8888)
      .onSuccess(http -> {
        System.out.println("HTTP server started on port 8888");
      });
  }

  private static void configureFlyway() {

    Flyway flyway = Flyway.configure()
      .dataSource("jdbc:postgresql://localhost:5432/edu3", "postgres", "4")
      .locations("classpath:db/migration")
      .load();

    flyway.migrate();
  }

  private void save(Pool client, RoutingContext routingContext) {
    JsonObject body = routingContext.body().asJsonObject();
    String name = body.getString("name");
    String email = body.getString("email");
    client.getConnection().compose(conn -> {
      return conn
        .preparedQuery("""
          INSERT INTO student (name, email) values ($1, $2) RETURNING id
          """)
        .execute(Tuple.of(name, email))
        .onComplete(arr -> conn.close());
    }).onSuccess(rows -> {
      Row row = rows.iterator().next();
      Long id = row.getLong("id");
      Student savedStudent = new Student();
      savedStudent.setId(id);
      savedStudent.setName(name);
      savedStudent.setName(email);

      JsonObject responseBody = new JsonObject();
      responseBody.put("id", id);
      responseBody.put("name", name);
      responseBody.put("email", email);

      routingContext.response()
        .setStatusCode(201)
        .putHeader("Content-Type", "application/json")
        .end(responseBody.encode());
    }).onFailure(error -> routingContext.response()
      .setStatusCode(500)
      .end("Error: " + error.getMessage()));

  }
}
