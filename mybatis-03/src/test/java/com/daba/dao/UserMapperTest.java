package com.daba.dao;

import com.daba.pojo.User;
import com.daba.utils.MybatisUtils;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

import java.util.List;

class UserMapperTest {
    @Test
    public void getUserById(){
        try(SqlSession sqlSession = MybatisUtils.getSqlSession()){
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);
            User user = mapper.getUserById(1); // Keep in mind that SQL is case insensitive
            System.out.println(user);
        }
    }
}