package com.mvo.edu_vert_x_app.repository;

import com.mvo.edu_vert_x_app.dto.request.StudentCourseTransientDTO;
import com.mvo.edu_vert_x_app.dto.request.StudentTransientDTO;
import com.mvo.edu_vert_x_app.entity.Student;
import com.mvo.edu_vert_x_app.entity.StudentCourse;
import com.mvo.edu_vert_x_app.exception.ApiException;
import com.mvo.edu_vert_x_app.mapper.StudentCourseMapper;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;

import java.util.*;

public class StudentCourseRepository {
  private final StudentCourseMapper studentCourseMapper;

  public StudentCourseRepository(StudentCourseMapper studentCourseMapper) {
    this.studentCourseMapper = studentCourseMapper;
  }

  public Future<StudentCourse> save(StudentCourseTransientDTO studentCourseTransientDTO, Pool client) {
    return client.withConnection(conn -> conn
      .preparedQuery("""
        INSERT INTO student_course (course_id, student_id)
        VALUES ($1,$2)
        RETURNING id, course_id, student_id
        """)
      .execute(Tuple.of(studentCourseTransientDTO.courseId(), studentCourseTransientDTO.studentId()))
      .onFailure(throwable -> {
        throw new ApiException(throwable.getMessage());
      })
      .map(studentCourseMapper::fromRowToStudentCourse)
    );
  }

  public Future<StudentCourse> save(StudentCourseTransientDTO studentCourseTransientDTO, SqlClient client) {
    return client
      .preparedQuery("""
        INSERT INTO student_course (course_id, student_id)
        VALUES ($1,$2)
        RETURNING id, course_id, student_id
        """)
      .execute(Tuple.of(studentCourseTransientDTO.courseId(), studentCourseTransientDTO.studentId()))
      .onFailure(throwable -> {
        throw new ApiException(throwable.getMessage());
      })
      .map(studentCourseMapper::fromRowToStudentCourse);
  }

  public Future<StudentCourse> getByStudentIdAndCourseId(Long studentId, Long courseId, Pool client) {
    return client.withConnection(conn -> conn
      .preparedQuery("""
        SELECT *
        FROM student_course
        WHERE student_id = $1
        AND course_id = $2
        """)
      .execute(Tuple.of(studentId, courseId))
      .map(studentCourseMapper::fromRowToStudentCourse)
    );
  }

  public Future<StudentCourse> getByStudentIdAndCourseId(Long studentId, Long courseId, SqlClient client) {
    return client
      .preparedQuery("""
        SELECT *
        FROM student_course
        WHERE student_id = $1
        AND course_id = $2
        """)
      .execute(Tuple.of(studentId, courseId))
      .map(studentCourseMapper::fromRowToStudentCourse);
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
