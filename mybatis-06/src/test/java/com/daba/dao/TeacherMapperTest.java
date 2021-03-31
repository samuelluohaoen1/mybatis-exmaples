package com.daba.dao;

import com.daba.pojo.Student;
import com.daba.pojo.Teacher;
import com.daba.utils.MybatisUtils;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TeacherMapperTest {

    @Test
    void getTeacher() {
        try(SqlSession sqlSession = MybatisUtils.getSqlSession()) {
            TeacherMapper mapper = sqlSession.getMapper(TeacherMapper.class);
            Teacher teacher = mapper.getTeacher(1);
            System.out.println(teacher);
        }
    }

    @Test
    void testStudent() {
        try(SqlSession sqlSession = MybatisUtils.getSqlSession()) {
            StudentMapper mapper = sqlSession.getMapper(StudentMapper.class);
            List<Student> students = mapper.getStudent();

            for (Student student : students) {
                System.out.println(student);
            }
        }
    }

    @Test
    void testStudent2() {
        try(SqlSession sqlSession = MybatisUtils.getSqlSession()) {
            StudentMapper mapper = sqlSession.getMapper(StudentMapper.class);
            List<Student> students = mapper.getStudent2();

            for (Student student : students) {
                System.out.println(student);
            }
        }
    }
}