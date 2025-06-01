package com.mvo.edu_vert_x_app.dto.response;

import com.mvo.edu_vert_x_app.dto.CourseDTO;

import java.util.Set;

public record ResponseStudentDTO(Long id, String name, String email, Set<CourseDTO> courses) {
}
