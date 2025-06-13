package com.mvo.edu_vert_x_app.repository;

import com.mvo.edu_vert_x_app.entity.StudentCourse;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

import java.util.*;

public class StudentCourseRepository {
  public Future<List<StudentCourse>> getByStudentId(Long studentId, Pool client) {
    return client
      .withConnection(conn ->
        conn.preparedQuery("""
            SELECT *
            FROM student_course
            WHERE student_id = $1
            """)
          .execute(Tuple.of(studentId))
          .map(rows -> {
            List<StudentCourse> studentCourseList = new ArrayList<>();
            for (Row row : rows) {
              StudentCourse sc = new StudentCourse();
              sc.setId(row.getLong("id"));
              sc.setStudentId(row.getLong("student_id"));
              sc.setCourseId(row.getLong("course_id"));
              studentCourseList.add(sc);
            }
            return studentCourseList;
          })
      );
  }
}
