package com.mvo.edu_vert_x_app.mapper;

import com.mvo.edu_vert_x_app.entity.Course;
import com.mvo.edu_vert_x_app.entity.Student;
import com.mvo.edu_vert_x_app.entity.StudentCourse;
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

  public Course fromRowToCourse(RowSet<Row> rows) {
    if (rows.size() == 0) {
      return new Course();
    }
    Row row = rows.iterator().next();
    Long id = row.getLong("id");
    String title = row.getString("title");
    Long teacherId = row.getLong("teacher_id");
    Course course = new Course();
    course.setId(id);
    course.setTitle(title);
    course.setTeacherId(teacherId);
    return course;
  }

}
