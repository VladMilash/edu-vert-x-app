package com.mvo.edu_vert_x_app.repository;

import com.mvo.edu_vert_x_app.entity.Course;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.util.*;

public class CourseRepository {
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
          .map(rows -> {
            List<Course> courseList = new ArrayList<>();
            for (Row row : rows) {
              Course course = new Course();
              course.setId(row.getLong("id"));
              course.setTitle(row.getString("title"));
              course.setTeacherId(row.getLong("teacher_id"));
              courseList.add(course);
            }
            return courseList;
          })
      );
  }
}
