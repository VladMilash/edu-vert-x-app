package com.mvo.edu_vert_x_app.controller;

import com.mvo.edu_vert_x_app.dto.request.StudentTransientDTO;
import com.mvo.edu_vert_x_app.service.StudentService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;

public class StudentController {
  private final Pool client;
  private final StudentService studentService;

  public StudentController(Pool client, StudentService studentService) {
    this.client = client;
    this.studentService = studentService;
  }

  public void saveStudent(RoutingContext context) {
    JsonObject body = context.body().asJsonObject();
    var studentTransientDTO = new StudentTransientDTO(body.getString("name"), body.getString("email"));
    studentService.save(studentTransientDTO, client)
      .onSuccess(savedStudent -> {
        JsonObject responseBody = new JsonObject();
        responseBody.put("id", savedStudent.id());
        responseBody.put("name", savedStudent.name());
        responseBody.put("email", savedStudent.email());
        responseBody.put("courses", savedStudent.courses());

        String locationUrl = "/api/v1/students/" + savedStudent.id();

        context.response()
          .setStatusCode(201)
          .putHeader("Content-Type", "application/json")
          .putHeader("Location", locationUrl)
          .end(responseBody.encode());
      }).onFailure(error -> context.response()
        .setStatusCode(500)
        .end("Error: " + error.getMessage()));
  }

  public void getById(RoutingContext context) {
    String stringId = context.pathParam("id");
    Long id = Long.valueOf(stringId);
    studentService.getById(id, client)
      .onSuccess(responseStudentDTO -> {
        JsonObject responseBody = new JsonObject();
        responseBody.put("id", responseStudentDTO.id());
        responseBody.put("name", responseStudentDTO.name());
        responseBody.put("email", responseStudentDTO.email());
        responseBody.put("courses", responseStudentDTO.courses());

        context.response()
          .setStatusCode(200)
          .putHeader("Content-Type", "application/json")
          .end(responseBody.encode());
      })
      .onFailure(error -> context.response()
        .setStatusCode(500)
        .end("Error: " + error.getMessage()));
  }
}
