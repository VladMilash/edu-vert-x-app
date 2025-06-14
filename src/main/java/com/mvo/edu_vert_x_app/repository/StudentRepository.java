package com.mvo.edu_vert_x_app.repository;

import com.mvo.edu_vert_x_app.dto.request.StudentTransientDTO;
import com.mvo.edu_vert_x_app.entity.Student;

import com.mvo.edu_vert_x_app.exception.NotFoundEntityException;
import com.mvo.edu_vert_x_app.mapper.StudentMapper;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Tuple;
import io.vertx.core.Future;

public class StudentRepository {
  private final StudentMapper studentMapper;

  public StudentRepository(StudentMapper studentMapper) {
    this.studentMapper = studentMapper;
  }

  public Future<Student> save(StudentTransientDTO studentTransientDTO, Pool client) {
    return client.getConnection().compose(conn -> conn
      .preparedQuery("""
        INSERT INTO student (name, email) values ($1, $2) RETURNING id, name, email
        """)
      .execute(Tuple.of(studentTransientDTO.name(), studentTransientDTO.email()))
      .onComplete(ar -> conn.close())
      .map(studentMapper::fromRowToStudent));
  }

  public Future<Student> getById(Long id, Pool client) {
    return client.getConnection().compose(conn -> conn
      .preparedQuery("""
        SELECT * FROM student
        WHERE id = $1
        """)
      .execute(Tuple.of(id))
      .onComplete(ar -> conn.close())
      .map(rows -> {
        if (rows.size() == 0) {
          throw new NotFoundEntityException(String.format("Student with id: %d not found", id));
        }
        return studentMapper.fromRowToStudent(rows);
      })
    );
  }
}



