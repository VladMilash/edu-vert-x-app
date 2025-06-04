package com.mvo.edu_vert_x_app;

import com.mvo.edu_vert_x_app.config.DbConfig;
import com.mvo.edu_vert_x_app.config.FlywayConfig;
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
  private FlywayConfig flywayConfig;


  @Override
  public Future<?> start() {
    dbConfig = new DbConfig(vertx);
    Pool client = getPool();

    flywayConfig = new FlywayConfig(vertx);
    configureFlyway();

    studentRepository = new StudentRepository();
    studentController = new StudentController(client,studentRepository);


    Router router = getRouter();

    router.post("/api/v1/students").handler(context -> studentController.saveStudent(context));

    return getHttpServerFuture(router);
  }

  private Pool getPool() {
    JsonObject properties = config().getJsonObject("reactive-connect-db");
    return dbConfig.getPool(properties);
  }

  private void configureFlyway() {
    JsonObject properties = config().getJsonObject("flyway");
    flywayConfig.configureFlyway(properties);
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


}
