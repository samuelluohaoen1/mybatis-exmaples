package com.daba.dao;

import com.daba.pojo.User;

import java.util.List;

public interface UserMapper {

    // Select a single entry by id
    User getUserById(int id);

}
