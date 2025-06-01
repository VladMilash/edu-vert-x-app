package com.mvo.edu_vert_x_app.repository;

import com.mvo.edu_vert_x_app.dto.request.StudentTransientDTO;
import com.mvo.edu_vert_x_app.entity.Student;

import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import io.vertx.core.Future;

public class StudentRepository {
  public Future<Student> save(StudentTransientDTO studentTransientDTO, Pool client) {
    return client.getConnection().compose(conn -> conn
      .preparedQuery("""
        INSERT INTO student (name, email) values ($1, $2) RETURNING id
        """)
      .execute(Tuple.of(studentTransientDTO.name(), studentTransientDTO.email()))
      .onComplete(ar -> conn.close())
      .map(rows -> {
        Row row = rows.iterator().next();
        Long id = row.getLong("id");
        Student savedStudent = new Student();
        savedStudent.setId(id);
        savedStudent.setName(studentTransientDTO.name());
        savedStudent.setEmail(studentTransientDTO.email());
        return savedStudent;
      }));
  }

}



