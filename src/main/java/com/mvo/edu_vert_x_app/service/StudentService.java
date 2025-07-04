package com.mvo.edu_vert_x_app.service;

import com.mvo.edu_vert_x_app.dto.request.StudentTransientDTO;
import com.mvo.edu_vert_x_app.dto.response.DeleteResponseDTO;
import com.mvo.edu_vert_x_app.dto.response.ResponseStudentDTO;
import io.vertx.sqlclient.Pool;
import java.util.*;

import io.vertx.core.Future;

public interface StudentService {
  Future<ResponseStudentDTO> save(StudentTransientDTO studentTransientDTO, Pool client);

  Future<ResponseStudentDTO> getById(Long id, Pool client);

  Future<List<ResponseStudentDTO>> getAll(int page, int size, Pool client);

  Future<ResponseStudentDTO> update(long id, StudentTransientDTO studentTransientDTO, Pool client);

  Future<DeleteResponseDTO> delete(Long id, Pool client);

}
