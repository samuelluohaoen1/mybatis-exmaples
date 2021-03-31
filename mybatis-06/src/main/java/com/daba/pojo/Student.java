package com.daba.pojo;

import lombok.Data;

@Data
public class Student {
    private int id;
    private String name;

    // Each Student needs to associate with a teacher
    private Teacher teacher;
}
