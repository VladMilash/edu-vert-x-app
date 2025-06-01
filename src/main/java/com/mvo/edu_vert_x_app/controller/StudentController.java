package com.mvo.edu_vert_x_app.controller;

import com.mvo.edu_vert_x_app.dto.request.StudentTransientDTO;
import com.mvo.edu_vert_x_app.repository.StudentRepository;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;

public class StudentController {
  private final Pool client;
  private final StudentRepository studentRepository;

  public StudentController(Pool client, StudentRepository studentRepository) {
    this.client = client;
    this.studentRepository = studentRepository;
  }


  public void saveStudent(RoutingContext context) {
    JsonObject body = context.body().asJsonObject();
    var studentTransientDTO = new StudentTransientDTO(body.getString("name"), body.getString("email"));
    studentRepository.save(studentTransientDTO, client)
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
  }
}
