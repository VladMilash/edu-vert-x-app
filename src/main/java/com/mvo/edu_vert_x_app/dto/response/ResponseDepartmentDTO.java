package com.mvo.edu_vert_x_app.dto.response;

import com.mvo.edu_vert_x_app.dto.TeacherDTO;

public record ResponseDepartmentDTO(Long id, String name, TeacherDTO headOfDepartment) {
}
