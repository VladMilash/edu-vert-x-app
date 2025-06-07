package com.mvo.edu_vert_x_app.service.impl;

import com.mvo.edu_vert_x_app.dto.request.StudentTransientDTO;
import com.mvo.edu_vert_x_app.dto.response.ResponseStudentDTO;
import com.mvo.edu_vert_x_app.mapper.StudentMapper;
import com.mvo.edu_vert_x_app.repository.StudentRepository;
import com.mvo.edu_vert_x_app.service.StudentService;
import io.vertx.sqlclient.Pool;

import io.vertx.core.Future;

public class StudentServiceImpl implements StudentService {
  private final StudentRepository studentRepository;
  private final StudentMapper studentMapper;

  public StudentServiceImpl(StudentRepository studentRepository, StudentMapper studentMapper) {
    this.studentRepository = studentRepository;
    this.studentMapper = studentMapper;
  }

  @Override
  public Future<ResponseStudentDTO> save(StudentTransientDTO studentTransientDTO, Pool client) {
    return studentRepository.save(studentTransientDTO, client)
      .map(studentMapper::fromStudentToResponseStudentDTO);
  }

  @Override
  public Future<ResponseStudentDTO> getById(Long id, Pool client) {
    return studentRepository.getById(id, client)
      .map(studentMapper::fromStudentToResponseStudentDTO);
  }
}
