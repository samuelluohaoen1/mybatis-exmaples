package com.daba.dao;

import com.daba.pojo.User;
import com.daba.utils.MybatisUtils;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserDaoTest {
    @Test
    public void test(){
        // == Step 1: Acquire sqlSession object ==
        /* Since SqlSession extends Closable which extends AutoClosable, we no
           longer need to manually call sqlSession.close(). It will automatically
           be called after the try-with-resources block ends. */
        try(SqlSession sqlSession = MybatisUtils.getSqlSession()){
            // == Step 2: Execute SQL ==
            // Method 1 (Officially Recommended):
            UserDao mapper = sqlSession.getMapper(UserDao.class);
            List<User> userList = mapper.getUserList();

            // Method 2 (Exists):
//        List<User> userList = sqlSession.selectList("com.daba.dao.UserDao.getUserList");

            for (User user : userList) {
                System.out.println(user);
            }
        }
        // Optional catch here

        // == Step 3: Shutdown session == Automatically done from using a try-with-resources.
    }
}