package com.mvo.edu_vert_x_app;

import com.mvo.edu_vert_x_app.config.DbConfig;
import com.mvo.edu_vert_x_app.controller.StudentController;
import com.mvo.edu_vert_x_app.repository.StudentRepository;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.sqlclient.Pool;
import org.flywaydb.core.Flyway;

public class MainVerticle extends VerticleBase {
  private StudentRepository studentRepository;
  private StudentController studentController;
  private DbConfig dbConfig;

  @Override
  public Future<?> start() {
    dbConfig = new DbConfig();
    Pool client = getPool();


    configureFlyway();

    studentRepository = new StudentRepository();
    studentController = new StudentController(client,studentRepository);


    Router router = getRouter();

    router.post("/api/v1/students").handler(context -> studentController.saveStudent(context));

    return getHttpServerFuture(router);
  }

  private Pool getPool() {
    JsonObject properties = config().getJsonObject("reactive-connect-db");
    return dbConfig.getPool(vertx,properties);
  }

  private Future<HttpServer> getHttpServerFuture(Router router) {
    return vertx.createHttpServer()
      .requestHandler(router)
      .listen(config().getInteger("HTTP_PORT", 8889))
      .onSuccess(http -> {
        System.out.println("HTTP server started");
      });
  }

  private Router getRouter() {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    return router;
  }


  private static void configureFlyway() {

    Flyway flyway = Flyway.configure()
      .dataSource("jdbc:postgresql://localhost:5432/edu3", "postgres", "4")
      .locations("classpath:db/migration")
      .load();

    flyway.migrate();
  }

}
