package com.mvo.edu_vert_x_app.entity;

import lombok.Data;

@Data
public class Course {
  private Long id;

  private String title;

  private Long teacherId;
}
