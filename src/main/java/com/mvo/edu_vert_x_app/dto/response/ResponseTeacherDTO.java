package com.mvo.edu_vert_x_app.dto.response;

import com.mvo.edu_vert_x_app.dto.CourseShortDTO;
import com.mvo.edu_vert_x_app.dto.DepartmentShortDTO;

import java.util.Set;

public record ResponseTeacherDTO
  (Long id, String name, Set<CourseShortDTO> courses, DepartmentShortDTO department){
}
