package com.mvo.edu_vert_x_app.service;

import com.mvo.edu_vert_x_app.dto.request.StudentTransientDTO;
import com.mvo.edu_vert_x_app.entity.Student;
import io.vertx.sqlclient.Pool;

public interface StudentService {
  Student save(StudentTransientDTO studentTransientDTO, Pool client);
}
