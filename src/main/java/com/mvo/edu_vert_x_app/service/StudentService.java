package com.mvo.edu_vert_x_app.service;

import com.mvo.edu_vert_x_app.dto.request.StudentTransientDTO;
import com.mvo.edu_vert_x_app.dto.response.ResponseStudentDTO;
import com.mvo.edu_vert_x_app.entity.Student;
import io.vertx.sqlclient.Pool;

import io.vertx.core.Future;

public interface StudentService {
  Future<ResponseStudentDTO> save(StudentTransientDTO studentTransientDTO, Pool client);

  Future<ResponseStudentDTO> getById(Long id, Pool client);

}
