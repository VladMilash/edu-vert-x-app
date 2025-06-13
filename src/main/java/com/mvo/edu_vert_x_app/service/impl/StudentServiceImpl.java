package com.mvo.edu_vert_x_app.service.impl;

import com.mvo.edu_vert_x_app.dto.CourseDTO;
import com.mvo.edu_vert_x_app.dto.TeacherDTO;
import com.mvo.edu_vert_x_app.dto.request.StudentTransientDTO;
import com.mvo.edu_vert_x_app.dto.response.ResponseStudentDTO;
import com.mvo.edu_vert_x_app.entity.Course;
import com.mvo.edu_vert_x_app.entity.Student;
import com.mvo.edu_vert_x_app.entity.StudentCourse;
import com.mvo.edu_vert_x_app.entity.Teacher;
import com.mvo.edu_vert_x_app.mapper.StudentMapper;
import com.mvo.edu_vert_x_app.repository.CourseRepository;
import com.mvo.edu_vert_x_app.repository.StudentCourseRepository;
import com.mvo.edu_vert_x_app.repository.StudentRepository;
import com.mvo.edu_vert_x_app.repository.TeacherRepository;
import com.mvo.edu_vert_x_app.service.StudentService;
import io.vertx.sqlclient.Pool;

import io.vertx.core.Future;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StudentServiceImpl implements StudentService {
  private final StudentRepository studentRepository;
  private final StudentMapper studentMapper;
  private final StudentCourseRepository studentCourseRepository;
  private final CourseRepository courseRepository;
  private final TeacherRepository teacherRepository;

  public StudentServiceImpl(StudentRepository studentRepository, StudentMapper studentMapper,
                            StudentCourseRepository studentCourseRepository, CourseRepository courseRepository,
                            TeacherRepository teacherRepository) {
    this.studentRepository = studentRepository;
    this.studentMapper = studentMapper;
    this.studentCourseRepository = studentCourseRepository;
    this.courseRepository = courseRepository;
    this.teacherRepository = teacherRepository;
  }

  @Override
  public Future<ResponseStudentDTO> save(StudentTransientDTO studentTransientDTO, Pool client) {
    return studentRepository.save(studentTransientDTO, client)
      .map(studentMapper::fromStudentToResponseStudentDTO);
  }

  @Override
  public Future<ResponseStudentDTO> getById(Long id, Pool client) {
    Future<Student> studentFuture = studentRepository.getById(id, client);
    Future<List<StudentCourse>> studentCourseListFuture = studentCourseRepository.getByStudentId(id, client);

    return Future.all(studentFuture, studentCourseListFuture)
      .flatMap(composite -> {
        Student student = composite.resultAt(0);
        List<StudentCourse> studentCourseList = composite.resultAt(1);

        if (studentCourseList.isEmpty()) {
          return Future.succeededFuture(studentMapper.fromStudentToResponseStudentDTO(student));
        }

        List<Long> coursesIds = studentCourseList
          .stream()
          .map(StudentCourse::getCourseId)
          .toList();
        return loadCoursesAndTeachersForStudent(student, coursesIds, client);
      });
  }

  private Future<ResponseStudentDTO> loadCoursesAndTeachersForStudent(Student student, List<Long> coursesIds, Pool client) {
    Future<List<Course>> courseListFuture = courseRepository.getByIdIn(coursesIds, client);

    return Future.all(Collections.singletonList(courseListFuture))
      .flatMap(composite -> {
        List<Course> courseList = composite.resultAt(0);
        List<Long> teacherIds = courseList
          .stream()
          .map(Course::getTeacherId)
          .filter(Objects::nonNull)
          .toList();

        if (teacherIds.isEmpty()) {
          Set<CourseDTO> courseDTOS = courseList
            .stream()
            .map(course -> {
              return new CourseDTO(course.getId(), course.getTitle(), null);
            })
            .collect(Collectors.toSet());
          return Future.succeededFuture(new ResponseStudentDTO(student.getId(), student.getName(), student.getEmail(), courseDTOS));
        }
        return loadTeacherForCourses(student, courseList, teacherIds, client);
      });
  }

  private Future<ResponseStudentDTO> loadTeacherForCourses(Student student, List<Course> courseList, List<Long> teacherIds, Pool client) {
    Future<List<Teacher>> teachersListFuture = teacherRepository.getByIdIn(teacherIds, client);

    return Future.all(Collections.singletonList(teachersListFuture))
      .flatMap(composite -> {
        List<Teacher> teacherList = composite.resultAt(0);
        Map<Long, Teacher> teacherMap = teacherList
          .stream()
          .collect(Collectors.toMap(Teacher::getId, Function.identity()));

        Set<CourseDTO> courseDTOS = courseList
          .stream()
          .map(course -> {
            if (course.getTeacherId() != null && teacherMap.containsKey(course.getTeacherId())) {
              Long teacherId = course.getTeacherId();
              Teacher teacher = teacherMap.get(teacherId);
              TeacherDTO teacherDTO = new TeacherDTO(
                teacher.getId(),
                teacher.getName()
              );
              return new CourseDTO(
                course.getId(),
                course.getTitle(),
                teacherDTO
              );
            }

            return new CourseDTO(
              course.getId(),
              course.getTitle(),
              null
            );
          })
          .collect(Collectors.toSet());
        return Future.succeededFuture(new ResponseStudentDTO(student.getId(), student.getName(), student.getEmail(), courseDTOS));

      });
  }
}
