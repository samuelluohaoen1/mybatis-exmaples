package com.daba.dao;

import com.daba.pojo.Teacher;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TeacherMapper {

    // Get all teachers
    List<Teacher> getTeachers();

    // Get a single teacher by ID and their students
    Teacher getTeacherById(int id);

    // Get a single teacher by ID and their students
    Teacher getTeacherById2(int id);
}
