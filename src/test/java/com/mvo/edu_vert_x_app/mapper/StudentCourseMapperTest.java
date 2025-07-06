package com.mvo.edu_vert_x_app.mapper;

import com.mvo.edu_vert_x_app.entity.StudentCourse;
import io.vertx.sqlclient.PropertyKind;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.desc.ColumnDescriptor;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StudentCourseMapperTest {
  private StudentCourseMapper studentCourseMapper;

  @BeforeEach
  void setUp() {
    studentCourseMapper = new StudentCourseMapper();
  }

  @Test
  @DisplayName("Test mapping empty row to StudentCourse")
  public void givenEmptyRowSet_whenFromRowToStudentCourse_thenTransientStudentCourse() {
    // given
    RowSet<Row> emptyRowSet = mock(RowSet.class);
    RowIterator<Row> emptyIterator = mock(RowIterator.class);
    when(emptyRowSet.size()).thenReturn(0);
    when(emptyRowSet.iterator()).thenReturn(emptyIterator);
    when(emptyIterator.hasNext()).thenReturn(false);

    // when
    StudentCourse studentCourse = studentCourseMapper.fromRowToStudentCourse(emptyRowSet);

    //then
    assertNotNull(studentCourse);
    assertNull(studentCourse.getId());
  }

}
