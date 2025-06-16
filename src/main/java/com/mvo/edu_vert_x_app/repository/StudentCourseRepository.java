package com.mvo.edu_vert_x_app.repository;

import com.mvo.edu_vert_x_app.entity.StudentCourse;
import com.mvo.edu_vert_x_app.mapper.StudentCourseMapper;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Tuple;

import java.util.*;

public class StudentCourseRepository {
  private final StudentCourseMapper studentCourseMapper;

  public StudentCourseRepository(StudentCourseMapper studentCourseMapper) {
    this.studentCourseMapper = studentCourseMapper;
  }

  public Future<List<StudentCourse>> getByStudentId(Long studentId, Pool client) {
    return client
      .withConnection(conn ->
        conn.preparedQuery("""
            SELECT *
            FROM student_course
            WHERE student_id = $1
            """)
          .execute(Tuple.of(studentId))
          .map(studentCourseMapper::fromRowsToStudentCoursesList)
      );
  }

  public Future<List<StudentCourse>> getByStudentIdIn(List<Long> studentsIds, Pool client) {
    if (studentsIds.isEmpty()) {
      return Future.succeededFuture(Collections.emptyList());
    }
    String sql = "SELECT * FROM student_course WHERE student_id = ANY($1::bigint[])";
    Tuple params = Tuple.of(studentsIds.toArray(new Long[0]));
    return client
      .withConnection(conn ->
        conn.preparedQuery(sql).execute(params)
          .map(studentCourseMapper::fromRowsToStudentCoursesList)
      );
  }
}
