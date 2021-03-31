package com.daba.dao;

import com.daba.pojo.User;
import com.daba.utils.MybatisUtils;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

import java.util.List;

class UserMapperTest {
    @Test
    public void getUsers() {
        try (SqlSession sqlSession = MybatisUtils.getSqlSession()) {
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);
            List<User> users = mapper.getUsers(); // Keep in mind that SQL is case insensitive
            for (var user : users) {
                System.out.println(user);
            }
        }
    }

    @Test
    public void getUserById(){
        try (SqlSession sqlSession = MybatisUtils.getSqlSession()) {
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);
            User user = mapper.getUserById(1); // Keep in mind that SQL is case insensitive
            System.out.println(user);
        }
    }

    @Test
    public void updateUserById(){
        try (SqlSession sqlSession = MybatisUtils.getSqlSession()) {
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);

            int n = mapper.updateUserById(new User(3, "daba", "980512")); // Keep in mind that SQL is case insensitive
            if(n > 0){
                System.out.println("Update successful!");
            }

            sqlSession.commit();
        }
    }
}