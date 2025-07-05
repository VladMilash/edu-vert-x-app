package com.mvo.edu_vert_x_app.controller;

import com.mvo.edu_vert_x_app.dto.CourseDTO;
import com.mvo.edu_vert_x_app.dto.request.StudentTransientDTO;
import com.mvo.edu_vert_x_app.dto.response.ResponseStudentDTO;
import com.mvo.edu_vert_x_app.exception.BadRequestException;
import com.mvo.edu_vert_x_app.service.StudentService;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;

import java.util.Collection;

public class StudentController {
  private final Pool client;
  private final StudentService studentService;

  public StudentController(Pool client, StudentService studentService) {
    this.client = client;
    this.studentService = studentService;
  }

  public void getStudentCourses(RoutingContext context) {
    String stringId = context.pathParam("id");
    Long id = Long.valueOf(stringId);
    studentService.getStudentCourses(id, client)
      .onSuccess(courseDTOS -> {
        buildResponseBodyAsCourseDTOJsonArray(context, courseDTOS);
      })
      .onFailure(context::fail);
  }

  public void deleteStudent(RoutingContext context) {
    String stringId = context.pathParam("id");
    Long id = Long.valueOf(stringId);
    studentService.delete(id, client)
      .onSuccess(deleteResponseDTO -> {
        JsonObject responseBody = new JsonObject();
        responseBody.put("message", deleteResponseDTO.message());
        context.response()
          .setStatusCode(200)
          .putHeader("Content-Type", "application/json")
          .end(responseBody.encode());
      })
      .onFailure(context::fail);

  }

  public void updateStudent(RoutingContext context) {
    JsonObject body = context.body().asJsonObject();
    var studentTransientDTO = new StudentTransientDTO(body.getString("name"), body.getString("email"));
    String stringId = context.pathParam("id");
    Long id = Long.valueOf(stringId);
    studentService.update(id, studentTransientDTO, client)
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
      .onFailure(context::fail);

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
      .onFailure(context::fail);
  }

  public void getAll(RoutingContext context) {
    int page = getIntParam(context, "page", 0);
    int size = getIntParam(context, "size", 10);

    studentService.getAll(page, size, client)
      .onSuccess(responseStudentDTOS -> {
        buildResponseBodyAsResponseStudentDTOJsonArray(context, responseStudentDTOS);
      })
      .onFailure(context::fail);
  }

  private void buildResponseBodyAsCourseDTOJsonArray(RoutingContext context, Collection<CourseDTO> courseDTOS) {
    JsonArray jsonArray = convertCoursesToJson(courseDTOS);
    context.response()
      .setStatusCode(200)
      .putHeader("Content-Type", "application/json")
      .end(jsonArray.encode());
  }

  private void buildResponseBodyAsResponseStudentDTOJsonArray(RoutingContext context, Collection<ResponseStudentDTO> responseStudentDTOS) {
    JsonArray jsonArray = new JsonArray();
    responseStudentDTOS.forEach(studentDTO ->
      jsonArray.add(new JsonObject()
          .put("id", studentDTO.id())
          .put("name", studentDTO.name())
          .put("email", studentDTO.email())
          .put("courses", convertCoursesToJson(studentDTO.courses()))
      )
    );
    context.response()
      .setStatusCode(200)
      .putHeader("Content-Type", "application/json")
      .end(jsonArray.encode());
  }

  private JsonArray convertCoursesToJson(Collection<CourseDTO> courses) {
    JsonArray jsonArray = new JsonArray();
    if (courses.isEmpty()) {
      return jsonArray;
    }
    courses.forEach(courseDTO ->
      jsonArray.add(
        new JsonObject()
          .put("id", courseDTO.id())
          .put("title", courseDTO.title())
          .put("teacher",
            courseDTO.teacher() == null
              ? null
              : new JsonObject()
              .put("id", courseDTO.teacher().id())
              .put("name", courseDTO.teacher().name())
          )
      )
    );
    return jsonArray;
  }

  private int getIntParam(RoutingContext context, String paramName, int defaultValue) {
    String param = context.queryParam(paramName)
      .stream()
      .findFirst()
      .orElse(null);

    if ((param == null) || param.isEmpty()) {
      return defaultValue;
    }

    try {
      return Integer.parseInt(param);
    } catch (NumberFormatException e) {
      context.fail(new BadRequestException(String.format("Invalid %s value %s", paramName, param)));
      return defaultValue;
    }
  }
}
