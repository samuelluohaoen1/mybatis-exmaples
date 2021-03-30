package com.daba.dao;

import com.daba.pojo.User;
import com.daba.utils.MybatisUtils;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

import java.util.List;

class UserMapperTest {
    @Test
    public void selectAllUsersInTable(){
        // == Step 1: Acquire sqlSession object ==
        /* Since SqlSession extends Closable which extends AutoClosable, we no
           longer need to manually call sqlSession.close(). It will automatically
           be called after the try-with-resources block ends. */
        try(SqlSession sqlSession = MybatisUtils.getSqlSession()){
            // == Step 2: Execute SQL ==
            // Method 1 (Officially Recommended):
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);
            List<User> userList = mapper.getUserList();

            // Method 2 (Exists):
//        List<User> userList = sqlSession.selectList("com.daba.dao.UserMapper.getUserList");

            for (User user : userList) {
                System.out.println(user);
            }
        }
        // Optional catch here

        // == Step 3: Shutdown session == Automatically done from using a try-with-resources.
    }

    @Test
    public void selectSingleUserById(){
        try(SqlSession sqlSession = MybatisUtils.getSqlSession()){
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);
            User user = mapper.getUserById(1);
            System.out.println(user);
        }
    }

    @Test
    public void insertUser(){
        try(SqlSession sqlSession = MybatisUtils.getSqlSession()){
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);
            int twenty = mapper.insertUser(new User("Twenty", "971012"));

            if (twenty > 0){
                System.out.println("Insert successful!");
            }

            // CUD operations need to be committed!
            sqlSession.commit();
        }
    }

    @Test
    public void updateUser(){
        try(SqlSession sqlSession = MybatisUtils.getSqlSession()){
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);
            int twenty = mapper.updateUser(new User(2, "Mark", "3344985"));

            if (twenty > 0){
                System.out.println("Update successful!");
            }

            // CUD operations need to be committed!
            sqlSession.commit();
        }
    }

    @Test
    public void deleteUser(){
        try(SqlSession sqlSession = MybatisUtils.getSqlSession()){
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);
            int twenty = mapper.deleteUser(8);

            if (twenty > 0){
                System.out.println("Delete successful!");
            }

            // CUD operations need to be committed!
            sqlSession.commit();
        }
    }

    @Test
    public void getUsersLike(){
        try(SqlSession sqlSession = MybatisUtils.getSqlSession()){
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);
            List<User> usersLike = mapper.getUsersLike("%M%"); // Keep in mind that SQL is case insensitive
            System.out.println(usersLike);
        }
    }
}