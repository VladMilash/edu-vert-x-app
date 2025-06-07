package com.mvo.edu_vert_x_app.mapper;

import com.mvo.edu_vert_x_app.dto.response.ResponseStudentDTO;
import com.mvo.edu_vert_x_app.entity.Student;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

import java.util.HashSet;

public class StudentMapper {
  public Student fromRowToStudent(RowSet<Row> rows) {
    Row row = rows.iterator().next();
    Long id = row.getLong("id");
    String name = row.getString("name");
    String email = row.getString("email");
    Student savedStudent = new Student();
    savedStudent.setId(id);
    savedStudent.setName(name);
    savedStudent.setEmail(email);
    return savedStudent;
  }

  public ResponseStudentDTO fromStudentToResponseStudentDTO(Student student) {
    return new ResponseStudentDTO(
      student.getId(),
      student.getName(),
      student.getEmail(),
      new HashSet<>()
    );
  }
}
