package com.mvo.edu_vert_x_app.repository;

import com.mvo.edu_vert_x_app.entity.Teacher;
import com.mvo.edu_vert_x_app.mapper.TeacherMapper;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Tuple;

import java.util.List;

public class TeacherRepository {
  private final TeacherMapper teacherMapper;

  public TeacherRepository(TeacherMapper teacherMapper) {
    this.teacherMapper = teacherMapper;
  }

  public Future<List<Teacher>> getByIdIn(List<Long> teacherIds, Pool client) {
    String sql = "SELECT * FROM teacher WHERE id = ANY($1::bigint[])";
    Tuple params = Tuple.of(teacherIds.toArray(new Long[0]));
    return client
      .withConnection(conn ->
        conn.preparedQuery(sql).execute(params)
          .map(teacherMapper::fromRowsToTechersList)
      );
  }

  public Future<Teacher> getById(Long teacherId, Pool client) {
    return client.withConnection(conn -> conn
      .preparedQuery("""
        SELECT * FROM teacher
        WHERE id = $1
        """)
      .execute(Tuple.of(teacherId))
      .map(teacherMapper::fromRowToTeacher)
    );
  }
}
