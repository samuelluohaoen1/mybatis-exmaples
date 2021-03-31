package com.daba.dao;

import com.daba.pojo.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface UserMapper {

    @Select("SELECT id, name, pwd as password FROM mybatis.user;")
    List<User> getUsers();

    // Now that we do not have a Mapper XML, we specify parameters used in SQL
    // using the @Param annotation
    @Select("SELECT id, name, pwd as password FROM mybatis.user WHERE id=#{id};")
    User getUserById(@Param("id") int id);

    // Like the example for previous modules, if the parameter is a custom class reference,
    // no need to use @Param
    @Update("UPDATE mybatis.user SET name=#{name}, pwd=#{password} WHERE id = #{id};")
    int updateUserById(User user);
}
