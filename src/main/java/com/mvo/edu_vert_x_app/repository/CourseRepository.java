package com.mvo.edu_vert_x_app.repository;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mvo.edu_vert_x_app.dto.request.CourseTransientDTO;
import com.mvo.edu_vert_x_app.entity.Course;
import com.mvo.edu_vert_x_app.exception.NotFoundEntityException;
import com.mvo.edu_vert_x_app.mapper.CourseMapper;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;

import java.util.*;

public class CourseRepository {
  private final CourseMapper courseMapper;

  public CourseRepository(CourseMapper courseMapper) {
    this.courseMapper = courseMapper;
  }

  public Future<Course> save(CourseTransientDTO courseTransientDTO, Pool client) {
    return client.withConnection(conn -> conn
      .preparedQuery("""
        INSERT INTO course (title)
        VALUES ($1)
        RETURNING id, title, teacher_id
        """)
      .execute(Tuple.of(courseTransientDTO.title()))
      .map(courseMapper::fromRowToCourse)
    );
  }

  public Future<List<Course>> getByIdIn(List<Long> courseIds, Pool client) {
    if (courseIds.isEmpty()) {
      return Future.succeededFuture(Collections.emptyList());
    }
    String sql = "SELECT * FROM course WHERE id = ANY($1::bigint[])";
    Tuple params = Tuple.of(courseIds.toArray(new Long[0]));
    System.out.println("Executing SQL: " + sql);
    System.out.println("With params: " + Arrays.toString(courseIds.toArray()));
    return client
      .withConnection(conn ->
        conn.preparedQuery(sql).execute(params)
          .map(courseMapper::fromRowsToCoursesList)
      );
  }

  public Future<Course> getById(Long id, Pool client) {
    return client.withConnection(conn -> conn
      .preparedQuery("""
        SELECT *
        FROM course
        WHERE id = $1
        """)
      .execute(Tuple.of(id))
      .map(rows -> {
        if (rows.size() == 0) {
          throw new NotFoundEntityException(String.format("Course with id: %d not found", id));
        }
        return courseMapper.fromRowToCourse(rows);
      })
    );
  }

  public Future<Course> getById(Long id, SqlClient client) {
    return client
      .preparedQuery("""
        SELECT *
        FROM course
        WHERE id = $1
        """)
      .execute(Tuple.of(id))
      .map(rows -> {
        if (rows.size() == 0) {
          throw new NotFoundEntityException(String.format("Course with id: %d not found", id));
        }
        return courseMapper.fromRowToCourse(rows);
      });
  }

}
