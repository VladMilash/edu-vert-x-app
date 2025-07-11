package com.mvo.edu_vert_x_app.controller;

import com.mvo.edu_vert_x_app.dto.request.CourseTransientDTO;
import com.mvo.edu_vert_x_app.service.CourseService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;

public class CourseController {
  private final Pool client;
  private final CourseService courseService;

  public CourseController(Pool client, CourseService courseService) {
    this.client = client;
    this.courseService = courseService;
  }

  public void saveCourse(RoutingContext context) {
    JsonObject body = context.body().asJsonObject();
    var courseTransientDTO = new CourseTransientDTO(body.getString("title"));
    courseService.save(courseTransientDTO, client)
      .onSuccess(responseCoursesDTO -> {
        JsonObject responseBody = new JsonObject();
        responseBody.put("id", responseCoursesDTO.id());
        responseBody.put("title", responseCoursesDTO.title());
        responseBody.put("teacher", responseCoursesDTO.teacher());
        responseBody.put("students", responseCoursesDTO.students());

        String locationUrl = "/api/v1/courses/" + responseCoursesDTO.id();

        context.response()
          .setStatusCode(201)
          .putHeader("Content-Type", "application/json")
          .putHeader("Location", locationUrl)
          .end(responseBody.encode());
      })
      .onFailure(context::fail);
  }
}
