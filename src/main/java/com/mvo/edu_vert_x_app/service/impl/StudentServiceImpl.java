package com.mvo.edu_vert_x_app.service.impl;

import com.mvo.edu_vert_x_app.dto.CourseDTO;
import com.mvo.edu_vert_x_app.dto.TeacherDTO;
import com.mvo.edu_vert_x_app.dto.request.StudentTransientDTO;
import com.mvo.edu_vert_x_app.dto.response.DeleteResponseDTO;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StudentServiceImpl implements StudentService {
  private static final Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);
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
  public Future<List<CourseDTO>> getStudentCourses(Long id, Pool client) {
    return studentRepository.getById(id, client)
      .compose(student -> {
        return studentCourseRepository.getByStudentId(id, client)
          .compose(studentCourseList -> {
            List<Long> courseIds = getEntityIdsAsList(studentCourseList, StudentCourse::getCourseId);
            if (courseIds.isEmpty()) {
              return Future.succeededFuture(Collections.emptyList());
            }
            return courseRepository.getByIdIn(courseIds, client)
              .compose(courses -> {
                List<Long> teacherIds = getEntityIdsAsList(courses, Course::getTeacherId);
                return teacherRepository.getByIdIn(teacherIds, client)
                  .compose(teachers -> {
                    List<CourseDTO> courseDTOS = getCourseDTOasList(courses, teachers);
                    return Future.succeededFuture(courseDTOS);
                  });
              });
          });
      });
  }

  @Override
  public Future<DeleteResponseDTO> delete(Long id, Pool client) {
    return studentRepository.getById(id, client)
      .compose(student -> studentRepository.delete(id,client)
        .map(new DeleteResponseDTO("Student deleted successfully")));
  }

  @Override
  public Future<ResponseStudentDTO> update(long id, StudentTransientDTO studentTransientDTO, Pool client) {
    return studentRepository.getById(id, client)
      .compose(student -> {
        student.setName(studentTransientDTO.name());
        student.setEmail(studentTransientDTO.email());
        return studentRepository.update(student, client)
          .compose(v -> getById(id, client));
      });
  }

  @Override
  public Future<ResponseStudentDTO> save(StudentTransientDTO studentTransientDTO, Pool client) {
    logger.info("Saving student with email: {}", studentTransientDTO.email());
    return studentRepository.save(studentTransientDTO, client)
      .map(studentMapper::fromStudentToResponseStudentDTO)
      .onSuccess(studentDTO -> logger.info("Student successfully created with id: {}", studentDTO.id()))
      .onFailure(error -> logger.error("Failed to saving student", error));
  }

  @Override
  public Future<ResponseStudentDTO> getById(Long id, Pool client) {
    Future<Student> studentFuture = studentRepository.getById(id, client);
    Future<List<StudentCourse>> studentCourseListFuture = studentCourseRepository.getByStudentId(id, client);

    return Future.all(studentFuture, studentCourseListFuture)
      .flatMap(composite -> {
        Student student = composite.resultAt(0);
        List<StudentCourse> studentCourseList = composite.resultAt(1);
        return loadStudentData(student, studentCourseList, client);
      });
  }
  @Override
  public Future<List<ResponseStudentDTO>> getAll(int page, int size, Pool client) {
    int offset = page * size;
    return studentRepository.getAll(size, offset, client)
      .compose(students -> {
        List<Long> studentsIds = getEntityIdsAsList(students, Student::getId);
        if (studentsIds.isEmpty()) {
          return Future.succeededFuture(Collections.emptyList());
        }
        return studentCourseRepository.getByStudentIdIn(studentsIds, client)
          .compose(studentCourseList -> loadStudentData(students, studentCourseList, client));
      });
  }

  private static List<CourseDTO> getCourseDTOasList(List<Course> courses, List<Teacher> teachers) {
    Map<Long, Teacher> teacherMap = getEntityMap(teachers, Teacher::getId);
    return courses.stream()
      .map(course -> getCourseDTO(course, teacherMap)).toList();
  }

  private Future<List<ResponseStudentDTO>> loadStudentData(List<Student> students,
                                                           List<StudentCourse> studentCourseList,
                                                           Pool client) {
    if (studentCourseList.isEmpty()) {
      return Future.succeededFuture(studentMapper.fromStudentToResponseStudentDTO(students));
    }
    List<Long> courseIds = getEntityIdsAsList(studentCourseList, StudentCourse::getCourseId);
    return courseRepository.getByIdIn(courseIds, client)
      .compose(courses -> {
        List<Long> teacherIds = getEntityIdsAsList(courses, Course::getTeacherId);
        return teacherRepository.getByIdIn(teacherIds, client)
          .compose(teachers -> {
            List<ResponseStudentDTO> responseStudentDTOS = mapToResponseStudentDTOs(students, courses, teachers, studentCourseList);
            return Future.succeededFuture(responseStudentDTOS);
          });
      });
  }

  private static List<ResponseStudentDTO> mapToResponseStudentDTOs(List<Student> students,
                                                                   List<Course> courses,
                                                                   List<Teacher> teachers,
                                                                   List<StudentCourse> studentCourseList) {
    Map<Long, List<Long>> studentToCourseIds = getMapStudentToCourseIds(studentCourseList);
    return students.stream()
      .map(student -> {
        Set<CourseDTO> courseDTOSet = getCourseDTOSet(student, studentToCourseIds, courses, teachers);
        return getResponseStudentDTO(student, courseDTOSet);
      }).toList();
  }

  private static Set<CourseDTO> getCourseDTOSet(Student student,
                                                Map<Long, List<Long>> studentToCourseIds,
                                                List<Course> courses,
                                                List<Teacher> teachers) {
    Map<Long, Teacher> teacherMap = getEntityMap(teachers, Teacher::getId);
    Map<Long, Course> courseMap = getEntityMap(courses, Course::getId);
    return studentToCourseIds.getOrDefault(student.getId(), Collections.emptyList())
      .stream()
      .map(courseMap::get)
      .filter(Objects::nonNull)
      .map(course -> getCourseDTO(course, teacherMap))
      .collect(Collectors.toSet());
  }

  private static Map<Long, List<Long>> getMapStudentToCourseIds(List<StudentCourse> studentCourseList) {
    return studentCourseList
      .stream()
      .collect(Collectors.groupingBy(
        StudentCourse::getStudentId,
        Collectors.mapping(StudentCourse::getCourseId, Collectors.toList())
      ));
  }

  private Future<ResponseStudentDTO> loadStudentData(Student student,
                                                     List<StudentCourse> studentCourseList,
                                                     Pool client) {
    if (studentCourseList.isEmpty()) {
      return Future.succeededFuture(studentMapper.fromStudentToResponseStudentDTO(student));
    }

    List<Long> coursesIds = getEntityIdsAsList(studentCourseList,StudentCourse::getCourseId);

    return courseRepository.getByIdIn(coursesIds, client)
      .compose(courses -> {
        List<Long> teachersIds = getEntityIdsAsList(courses,Course::getTeacherId);
        return teacherRepository.getByIdIn(teachersIds, client)
          .compose(teachers -> {
            Set<CourseDTO> courseDTOS = getCourseDTOSet(teachers, courses);
            return Future.succeededFuture(getResponseStudentDTO(student, courseDTOS));
          });
      });
  }

  private static <T> Map<Long, T> getEntityMap(List<T> entities, Function<T, Long> function) {
    return entities
      .stream()
      .collect(Collectors.toMap(function, Function.identity()));
  }


  private static <T> List<Long> getEntityIdsAsList(List<T> entities, Function<T, Long> function) {
    return entities
      .stream()
      .map(function)
      .toList();
  }

  private static Set<CourseDTO> getCourseDTOSet(List<Teacher> teachers, List<Course> courses) {
    Map<Long, Teacher> teacherMap = getEntityMap(teachers, Teacher::getId);
    return courses
      .stream()
      .map(course -> getCourseDTO(course, teacherMap))
      .collect(Collectors.toSet());
  }

  private static CourseDTO getCourseDTO(Course course) {
    return new CourseDTO(
      course.getId(),
      course.getTitle(),
      null
    );
  }

  private static CourseDTO getCourseDTO(Course course, Map<Long, Teacher> teacherMap) {
    if (course.getTeacherId() != null && teacherMap.containsKey(course.getTeacherId())) {
      Teacher teacher = teacherMap.get(course.getTeacherId());
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
    return getCourseDTO(course);
  }

  private static ResponseStudentDTO getResponseStudentDTO(Student student, Set<CourseDTO> courseDTOS) {
    return new ResponseStudentDTO(
      student.getId(),
      student.getName(),
      student.getEmail(),
      courseDTOS);
  }
}
