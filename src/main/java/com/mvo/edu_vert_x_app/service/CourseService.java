package com.mvo.edu_vert_x_app.service;

import com.mvo.edu_vert_x_app.dto.request.CourseTransientDTO;
import com.mvo.edu_vert_x_app.dto.response.ResponseCoursesDTO;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;

public interface CourseService {
  Future<ResponseCoursesDTO> save(CourseTransientDTO courseTransientDTO, Pool client);
  Future<ResponseCoursesDTO> getById(Long id, Pool client);
}
