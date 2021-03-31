package com.daba.pojo;

import lombok.Data;

import java.util.List;

@Data
public class Teacher {
    private int id;
    private String name;

    // One teacher has many students
    private List<Student> students;
}
