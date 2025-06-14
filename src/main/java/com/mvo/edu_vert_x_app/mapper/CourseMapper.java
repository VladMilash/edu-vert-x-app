package com.mvo.edu_vert_x_app.mapper;

import com.mvo.edu_vert_x_app.entity.Course;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import java.util.*;

public class CourseMapper {
  public List<Course> fromRowsToCoursesList(RowSet<Row> rows) {
    List<Course> courseList = new ArrayList<>();
    for (Row row : rows) {
      Course course = new Course();
      course.setId(row.getLong("id"));
      course.setTitle(row.getString("title"));
      course.setTeacherId(row.getLong("teacher_id"));
      courseList.add(course);
    }
    return courseList;
  }
}
