package com.mvo.edu_vert_x_app.service.impl;

import com.mvo.edu_vert_x_app.dto.request.StudentTransientDTO;
import com.mvo.edu_vert_x_app.entity.Student;
import com.mvo.edu_vert_x_app.repository.StudentRepository;
import com.mvo.edu_vert_x_app.service.StudentService;
import io.vertx.sqlclient.Pool;

public class StudentServiceImpl implements StudentService {
  private final StudentRepository studentRepository;

  public StudentServiceImpl(StudentRepository studentRepository) {
    this.studentRepository = studentRepository;
  }

  @Override
  public Student save(StudentTransientDTO studentTransientDTO, Pool client) {
    return null;
  }
}
