package com.mvo.edu_vert_x_app.service.impl;

import com.mvo.edu_vert_x_app.dto.CourseDTO;
import com.mvo.edu_vert_x_app.dto.TeacherDTO;
import com.mvo.edu_vert_x_app.dto.request.StudentCourseTransientDTO;
import com.mvo.edu_vert_x_app.dto.request.StudentTransientDTO;
import com.mvo.edu_vert_x_app.dto.response.DeleteResponseDTO;
import com.mvo.edu_vert_x_app.dto.response.ResponseStudentDTO;
import com.mvo.edu_vert_x_app.entity.Course;
import com.mvo.edu_vert_x_app.entity.Student;
import com.mvo.edu_vert_x_app.entity.StudentCourse;
import com.mvo.edu_vert_x_app.entity.Teacher;
import com.mvo.edu_vert_x_app.exception.AlReadyExistException;
import com.mvo.edu_vert_x_app.mapper.StudentMapper;
import com.mvo.edu_vert_x_app.repository.CourseRepository;
import com.mvo.edu_vert_x_app.repository.StudentCourseRepository;
import com.mvo.edu_vert_x_app.repository.StudentRepository;
import com.mvo.edu_vert_x_app.repository.TeacherRepository;
import com.mvo.edu_vert_x_app.service.StudentService;
import com.mvo.edu_vert_x_app.util.DbExecutor;
import io.vertx.sqlclient.Pool;

import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;
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
  public Future<ResponseStudentDTO> setRelationWithCourse(Long studentId, Long courseId, Pool client, DbExecutor dbExecutor) {
    logger.info("Starting setRelationWithCourse with student id: {}, course id: {}", studentId, courseId);
    return dbExecutor.execute(sqlConnection -> {
        logger.info("Founding student with id: {}", studentId);
        Future<Student> studentFuture = studentRepository.getById(studentId, sqlConnection);
        logger.info("Founding course with id: {}", courseId);
        Future<Course> courseFuture = courseRepository.getById(courseId, sqlConnection);
        return Future.all(studentFuture, courseFuture)
          .flatMap(composit -> studentCourseRepository.getByStudentIdAndCourseId(studentId, courseId, sqlConnection)
            .compose(studentCourse -> saveStudentCourseRelation(studentId, courseId, sqlConnection, studentCourse)));
      }).compose(sc -> getById(studentId, client))
      .onSuccess(responseStudentDTO -> logger.info("Successfully created relation with student id: {} and with course with id: {}", studentId, courseId))
      .onFailure(error -> logger.error("Failed to create relation relation with student id: {} and with course with id: {}", studentId, courseId, error));
  }

  @Override
  public Future<List<CourseDTO>> getStudentCourses(Long id, Pool client) {
    logger.info("Starting getStudentCourses with student id: {}", id);
    return studentRepository.getById(id, client)
      .compose(student -> studentCourseRepository.getByStudentId(id, client)
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
            })
            .onSuccess(courseDTOS -> logger.info("Successfully got courses for student with id: {}", id))
            .onFailure(error -> logger.error("Failed to get courses for student with id: {}", id, error));
        }));
  }

  @Override
  public Future<DeleteResponseDTO> delete(Long id, Pool client) {
    logger.info("Starting delete student with id: {}", id);
    return studentRepository.getById(id, client)
      .compose(student -> studentRepository.delete(id,client)
        .map(new DeleteResponseDTO("Student deleted successfully")))
      .onSuccess(deleteResponseDTO -> logger.info("Successfully deleted student with id: {}", id))
      .onFailure(error -> logger.error("Failed to delete student with id: {}", id, error));
  }

  @Override
  public Future<ResponseStudentDTO> update(long id, StudentTransientDTO studentTransientDTO, Pool client) {
    logger.info("Starting update student with id: {}. New student data: {}", id, studentTransientDTO.toString());
    return studentRepository.getById(id, client)
      .compose(student -> {
        student.setName(studentTransientDTO.name());
        student.setEmail(studentTransientDTO.email());
        return studentRepository.update(student, client)
          .compose(v -> getById(id, client));
      })
      .onSuccess(responseStudentDTO -> logger.info("Successfully update student with id: {}", id))
      .onFailure(error -> logger.error("Failed to update student with id: {}", id, error));
  }

  @Override
  public Future<ResponseStudentDTO> save(StudentTransientDTO studentTransientDTO, Pool client) {
    logger.info("Starting save student with email: {}", studentTransientDTO.email());
    return studentRepository.save(studentTransientDTO, client)
      .map(studentMapper::fromStudentToResponseStudentDTO)
      .onSuccess(responseStudentDTO -> logger.info("Student successfully created with id: {}", responseStudentDTO.id()))
      .onFailure(error -> logger.error("Failed to saving student", error));
  }

  @Override
  public Future<ResponseStudentDTO> getById(Long id, Pool client) {
    logger.info("Starting get student by id: {}", id);
    Future<Student> studentFuture = studentRepository.getById(id, client);
    Future<List<StudentCourse>> studentCourseListFuture = studentCourseRepository.getByStudentId(id, client);

    return Future.all(studentFuture, studentCourseListFuture)
      .flatMap(composite -> {
        Student student = composite.resultAt(0);
        List<StudentCourse> studentCourseList = composite.resultAt(1);
        return loadStudentData(student, studentCourseList, client);
      })
      .onSuccess(responseStudentDTO -> logger.info("Student with id: {} successfully got", id))
      .onFailure(error -> logger.error("Failed to get student with id: {}", id, error));
  }


  @Override
  public Future<List<ResponseStudentDTO>> getAll(int page, int size, Pool client) {
    logger.info("Starting get all students. Page: {}, size: {}", page, size);
    int offset = page * size;
    return studentRepository.getAll(size, offset, client)
      .compose(students -> {
        List<Long> studentsIds = getEntityIdsAsList(students, Student::getId);
        if (studentsIds.isEmpty()) {
          return Future.succeededFuture(Collections.emptyList());
        }
        return studentCourseRepository.getByStudentIdIn(studentsIds, client)
          .compose(studentCourseList -> loadStudentData(students, studentCourseList, client));
      })
      .onSuccess(responseStudentDTOList -> logger.info("Successfully got all students. Page: {}, size: {} ", page, size))
      .onFailure(error -> logger.error("Failed to get all students Page: {}, size: {}", page, size, error));
  }

  private Future<StudentCourse> saveStudentCourseRelation(Long studentId, Long courseId, SqlConnection sqlConnection, StudentCourse studentCourse) {
    logger.info("Starting saveStudentCourseRelation with student id: {}, course id: {}", studentId, courseId);
    if (studentCourse.getId() != null) {
      logger.warn("Relation with student {} and course {} already exist. StudentCourse id: {}", studentId, courseId, studentCourse.getId());
      return Future.failedFuture(new AlReadyExistException
        (String.format("Relation between student with id: %d and course with id: %d already exists", studentId, courseId)));
    }
    logger.info("Creating studentCourse with student id: {}, course id: {}", studentCourse, courseId);
    StudentCourseTransientDTO studentCourseTransientDTO = new StudentCourseTransientDTO(
      courseId,
      studentId
    );
    logger.info("Saving studentCourse with student id: {}, course id: {} in DB", studentCourse, courseId);
    return studentCourseRepository.save(studentCourseTransientDTO, sqlConnection)
      .onSuccess(sc -> logger.info("Successfully saved relation with student and course. Student id: {}, course id: {}", studentId, courseId))
      .onFailure(error -> logger.error("Failed to save relation with student and course. Student id: {}, course id: {}", studentId, courseId, error));
  }

  private static List<CourseDTO> getCourseDTOasList(List<Course> courses, List<Teacher> teachers) {
    Map<Long, Teacher> teacherMap = getEntityMap(teachers, Teacher::getId);
    return courses.stream()
      .map(course -> getCourseDTO(course, teacherMap)).toList();
  }

  private Future<List<ResponseStudentDTO>> loadStudentData(List<Student> students,
                                                           List<StudentCourse> studentCourseList,
                                                           Pool client) {
    logger.info("Starting load data for students");
    if (studentCourseList.isEmpty()) {
      logger.info("Students do not has courses");
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
      })
      .onSuccess(responseStudentDTOList -> logger.info("Successfully load data for students"))
      .onFailure(error -> logger.error("Failed to load data for students", error));
  }

  private static List<ResponseStudentDTO> mapToResponseStudentDTOs(List<Student> students,
                                                                   List<Course> courses,
                                                                   List<Teacher> teachers,
                                                                   List<StudentCourse> studentCourseList) {
    logger.info("Starting map students and data to ResponseStudentDTO");
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
    logger.info("Starting to create CourseDTOSet for student with id: {}", student.getId());
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
    logger.info("Starting map students id to course ids");
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
    logger.info("Starting load data for student with id: {}", student.getId());
    if (studentCourseList.isEmpty()) {
      logger.info("Student with id: {} do not has courses", student.getId());
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
      })
      .onSuccess(responseStudentDTO -> logger.info("Successfully load data for student with id: {}", student.getId()))
      .onFailure(error -> logger.error("Failed to load data for student with id: {}", student.getId(), error));
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
