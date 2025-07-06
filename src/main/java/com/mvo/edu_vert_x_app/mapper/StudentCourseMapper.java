package com.mvo.edu_vert_x_app.mapper;

import com.mvo.edu_vert_x_app.entity.Course;
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

  public StudentCourse fromRowToStudentCourse(RowSet<Row> rows) {
    if (rows.size() == 0) {
      return new StudentCourse();
    }
    Row row = rows.iterator().next();
    Long id = row.getLong("id");
    Long studentId = row.getLong("student_id");
    Long courseId = row.getLong("course_id");
    StudentCourse studentCourse = new StudentCourse();
    studentCourse.setId(id);
    studentCourse.setStudentId(studentId);
    studentCourse.setCourseId(courseId);
    return studentCourse;
  }
}
