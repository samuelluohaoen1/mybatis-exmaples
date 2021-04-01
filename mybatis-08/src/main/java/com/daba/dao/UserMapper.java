package com.daba.dao;

import com.daba.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    User selectUserById(@Param("id") int id);
}
