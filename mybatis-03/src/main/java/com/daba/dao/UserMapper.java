package com.daba.dao;

import com.daba.pojo.User;

import java.util.List;

public interface UserMapper {
    // Select all entries
    List<User> getUserList();

    // Select a single entry by id
    User getUserById(int id);

    // Insert a single User. CUD needs to commit transactions!
    int insertUser(User user);

    // Update a single user by id
    int updateUser(User user);

    // Delete a single user by id
    int deleteUser(int id);

}
