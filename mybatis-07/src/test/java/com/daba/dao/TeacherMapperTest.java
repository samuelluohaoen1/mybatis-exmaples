package com.daba.dao;

import com.daba.pojo.Teacher;
import com.daba.utils.MybatisUtils;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

import java.util.List;

class TeacherMapperTest {

    @Test
    void getTeachers() {
        try(SqlSession sqlSession = MybatisUtils.getSqlSession()){
            TeacherMapper mapper = sqlSession.getMapper(TeacherMapper.class);
            List<Teacher> teachers = mapper.getTeachers();

            for (Teacher teacher : teachers) {
                System.out.println(teacher);
            }
        }
    }

    @Test
    void getTeacher() {
        try(SqlSession sqlSession = MybatisUtils.getSqlSession()){
            TeacherMapper mapper = sqlSession.getMapper(TeacherMapper.class);
            Teacher teachers = mapper.getTeacherById(1);

            System.out.println(teachers);
        }
    }

    @Test
    void getTeacher2() {
        try(SqlSession sqlSession = MybatisUtils.getSqlSession()){
            TeacherMapper mapper = sqlSession.getMapper(TeacherMapper.class);
            Teacher teachers = mapper.getTeacherById2(1);

            System.out.println(teachers);
        }
    }
}