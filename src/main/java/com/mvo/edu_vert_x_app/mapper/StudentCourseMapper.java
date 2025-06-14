package com.mvo.edu_vert_x_app.mapper;

import com.mvo.edu_vert_x_app.entity.StudentCourse;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

import java.util.ArrayList;
import java.util.List;

public class StudentCourseMapper {

  public List<StudentCourse> fromRowsToStudentCoursesList(RowSet<Row> rows) {
    List<StudentCourse> studentCourseList = new ArrayList<>();
    for (Row row : rows) {
      StudentCourse sc = new StudentCourse();
      sc.setId(row.getLong("id"));
      sc.setStudentId(row.getLong("student_id"));
      sc.setCourseId(row.getLong("course_id"));
      studentCourseList.add(sc);
    }
    return studentCourseList;
  }
}
