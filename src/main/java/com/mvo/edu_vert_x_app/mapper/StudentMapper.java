package com.mvo.edu_vert_x_app.mapper;

import com.mvo.edu_vert_x_app.dto.response.ResponseStudentDTO;
import com.mvo.edu_vert_x_app.entity.Student;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class StudentMapper {
  public Student fromRowToStudent(RowSet<Row> rows) {
    Row row = rows.iterator().next();
    Long id = row.getLong("id");
    String name = row.getString("name");
    String email = row.getString("email");
    Student student = new Student();
    student.setId(id);
    student.setName(name);
    student.setEmail(email);
    return student;
  }

  public ResponseStudentDTO fromStudentToResponseStudentDTO(Student student) {
    return new ResponseStudentDTO(
      student.getId(),
      student.getName(),
      student.getEmail(),
      new HashSet<>()
    );
  }

  public List<Student> fromRowsToStudent(RowSet<Row> rows) {
    List<Student> studentList = new ArrayList<>();
    for (Row row : rows) {
      Long id = row.getLong("id");
      String name = row.getString("name");
      String email = row.getString("email");
      Student student = new Student();
      student.setId(id);
      student.setName(name);
      student.setEmail(email);
      studentList.add(student);
    }
    return studentList;
  }

  public List<ResponseStudentDTO> fromStudentToResponseStudentDTO(List<Student> students) {
    return  students
      .stream()
      .map(student -> {
        return new ResponseStudentDTO(
          student.getId(),
          student.getName(),
          student.getEmail(),
          new HashSet<>()
        );
      })
      .toList();
  }
}
