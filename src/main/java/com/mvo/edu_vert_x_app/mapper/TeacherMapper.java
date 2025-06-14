package com.mvo.edu_vert_x_app.mapper;

import com.mvo.edu_vert_x_app.entity.Teacher;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

import java.util.ArrayList;
import java.util.List;

public class TeacherMapper {
  public List<Teacher> fromRowsToTechersList(RowSet<Row> rows) {
    List<Teacher> teacherList = new ArrayList<>();
    for (Row row : rows) {
      Teacher teacher = new Teacher();
      teacher.setId(row.getLong("id"));
      teacher.setName(row.getString("name"));
      teacherList.add(teacher);
    }
    return teacherList;
  }
}
