package com.daba.dao;

import com.daba.pojo.User;
import com.daba.utils.MybatisUtils;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

class UserMapperTest {
    @Test
    public void getUserList(){
        try(SqlSession sqlSession = MybatisUtils.getSqlSession()){
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);
            List<User> userList = mapper.getUserList(); // Keep in mind that SQL is case insensitive
            for (var user : userList) {
                System.out.println(user);
            }
        }
    }
}