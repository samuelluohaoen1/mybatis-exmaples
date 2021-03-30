package com.daba.dao;

import com.daba.pojo.User;
import com.daba.utils.MybatisUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;


class UserMapperTest {
    static Logger logger = Logger.getLogger(UserMapperTest.class);

    @Test
    public void getUserById(){
        try(SqlSession sqlSession = MybatisUtils.getSqlSession()){
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);
            User user = mapper.getUserById(1); // Keep in mind that SQL is case insensitive
            System.out.println(user);
        }
    }

    @Test
    public void testLog4j(){
        logger.info("info: Executing testLog4j method.");
        logger.debug("debug: Executing testLog4j method.");
        logger.error("error: Executing testLog4j method.");
    }
}