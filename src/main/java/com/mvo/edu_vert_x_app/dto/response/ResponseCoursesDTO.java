package com.mvo.edu_vert_x_app.dto.response;

import com.mvo.edu_vert_x_app.dto.StudentDTO;
import com.mvo.edu_vert_x_app.dto.TeacherDTO;

import java.util.Set;

public record ResponseCoursesDTO
  (Long id, String title, TeacherDTO teacher, Set<StudentDTO> students){
}
