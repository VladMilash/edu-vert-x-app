package com.mvo.edu_vert_x_app.service.impl;

import com.mvo.edu_vert_x_app.dto.StudentDTO;
import com.mvo.edu_vert_x_app.dto.TeacherDTO;
import com.mvo.edu_vert_x_app.dto.request.CourseTransientDTO;
import com.mvo.edu_vert_x_app.dto.response.ResponseCoursesDTO;
import com.mvo.edu_vert_x_app.entity.StudentCourse;
import com.mvo.edu_vert_x_app.mapper.CourseMapper;
import com.mvo.edu_vert_x_app.repository.CourseRepository;
import com.mvo.edu_vert_x_app.repository.StudentCourseRepository;
import com.mvo.edu_vert_x_app.repository.StudentRepository;
import com.mvo.edu_vert_x_app.repository.TeacherRepository;
import com.mvo.edu_vert_x_app.service.CourseService;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class CourseServiceImpl implements CourseService {
  private final CourseRepository courseRepository;
  private final StudentRepository studentRepository;
  private final TeacherRepository teacherRepository;
  public final StudentCourseRepository studentCourseRepository;
  private final CourseMapper courseMapper;
  private static final Logger logger = LoggerFactory.getLogger(CourseServiceImpl.class);

  public CourseServiceImpl(CourseRepository courseRepository,
                           CourseMapper courseMapper,
                           StudentCourseRepository studentCourseRepository,
                           StudentRepository studentRepository,
                           TeacherRepository teacherRepository) {
    this.courseRepository = courseRepository;
    this.courseMapper = courseMapper;
    this.studentCourseRepository = studentCourseRepository;
    this.studentRepository = studentRepository;
    this.teacherRepository = teacherRepository;
  }

  @Override
  public Future<ResponseCoursesDTO> save(CourseTransientDTO courseTransientDTO, Pool client) {
    return courseRepository.save(courseTransientDTO, client)
      .map(courseMapper::fromCourseToResponseCourseDTO)
      .onSuccess(responseCoursesDTO -> logger.info("Course successfully created with id: {}", responseCoursesDTO.id()))
      .onFailure(error -> logger.error("Failed to saving course", error));
  }

  //TODO требуется рефакторинг. Возможно подойдут какие-то методы из сервиса студента (вынести в утилиты)
  @Override
  public Future<ResponseCoursesDTO> getById(Long id, Pool client) {
    return courseRepository.getById(id, client)
      .compose(course -> studentCourseRepository.getByCourseId(id, client)
       .compose(studentCourseList -> {
         List<Long> studentIds = studentCourseList
           .stream()
           .map(StudentCourse::getStudentId)
           .toList();
         return studentRepository.getByIdIn(studentIds, client)
           .compose(students -> {
             if (course.getTeacherId() != null) {
               return teacherRepository.getById(course.getTeacherId(), client)
                 .compose(teacher -> {
                   Set<StudentDTO> studentDTOSet = students.stream()
                     .map(student -> new StudentDTO(
                       student.getId(),
                       student.getName(),
                       student.getEmail()
                     ))
                     .collect(Collectors.toSet());
                   ResponseCoursesDTO responseCoursesDTO = new ResponseCoursesDTO(
                     course.getId(),
                     course.getTitle(),
                     teacher != null ?
                       new TeacherDTO(
                         teacher.getId(),
                         teacher.getName()
                       )
                       : null,
                     studentDTOSet
                   );
                   return Future.succeededFuture(responseCoursesDTO);
                 });
             } else {
               Set<StudentDTO> studentDTOSet = students.stream()
                 .map(student -> new StudentDTO(
                   student.getId(),
                   student.getName(),
                   student.getEmail()
                 ))
                 .collect(Collectors.toSet());
               ResponseCoursesDTO responseCoursesDTO = new ResponseCoursesDTO(
                 course.getId(),
                 course.getTitle(),
                 null,
                 studentDTOSet
               );
               return Future.succeededFuture(responseCoursesDTO);
             }
           });
       }));
  }
}
