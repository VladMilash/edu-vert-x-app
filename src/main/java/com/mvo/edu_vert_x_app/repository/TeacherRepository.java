package com.mvo.edu_vert_x_app.repository;

import com.mvo.edu_vert_x_app.entity.Teacher;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TeacherRepository {
  public Future<List<Teacher>> getByIdIn(List<Long> teacherIds, Pool client) {
    String sql = "SELECT * FROM teacher WHERE id = ANY($1::bigint[])";
    Tuple params = Tuple.of(teacherIds.toArray(new Long[0]));
    return client
      .withConnection(conn ->
        conn.preparedQuery(sql).execute(params)
          .map(rows -> {
            List<Teacher> teacherList = new ArrayList<>();
            for (Row row : rows) {
              Teacher teacher = new Teacher();
              teacher.setId(row.getLong("id"));
              teacher.setName(row.getString("name"));
              teacherList.add(teacher);
            }
            return teacherList;
          })
      );
  }
}
