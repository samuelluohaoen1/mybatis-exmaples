# 1. Introduction

In plain words, the goal of MyBatis is not to do fancy things, but to simply make the life of developers easier. Without MyBatis, Java developers had to use JDBC where they have to deal with trivial DB connectivity related logics. MyBatis now gets rid of the need of worrying about DB connectivity logics, thus, increases code readability and scalability.  Do note that anything you can achieve with MyBatis, you can achieve the same without MyBatis. --- Personal opinion - 仁者见仁智者见智。

# 2. A new MyBatis project

``The next few sections are discussed around Module mybatis-01`` 

Dependencies of this sample project:

* A pre-created mySQL database
* Jar dependencies:
  * mySQL Connector V8.0.22
  * MyBatis V3.5.5
  * JUnit 5 V5.7.0
  * For more details on Jar dependencies check root pom.xml

In general: Environment Setup --> MyBatis Setup --> Coding --> Testing

## 2.1. Environment Setup

* Create a database in mySQL like the following example:

```mysql
CREATE DATABASE IF NOT EXISTS mybatis;

USE mybatis;

CREATE TABLE IF NOT EXISTS user(
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(30) DEFAULT NULL,
    pwd VARCHAR(30) DEFAULT NULL
)DEFAULT CHARSET=utf8;

INSERT INTO user (name, pwd) VALUES
('Samuel', '123456'),
('Jason', '654321'),
('Mark', 'ppap');
```

* Retrive Jar files as Maven dependencies.
* Create "mybatis-config.xml" MyBatis Core config file.
* Create util class for retrieving sqlSessionFactory as recommended by MyBatis official documentation.

## 2.2. Coding

* Create an entity class.
* Create an Mapper (Essentially DAO) interface.
* Implement the Mapper interface: When only using JDBC, DAO interfaces had to be implemented and many DB connectivity issues need to be considered. When using MyBatis, those concerns are now being handled by MyBatis. Therefore, we only need to point MyBatis to the right direction by using a Mapper.xml i.e "UserMapper.xml".

## 2.3. Testing

**Note:** A couple very common exceptions:

* org.apache.ibatis.binding.BindingException: Type interface com.daba.dao.UserDao is not known to the MapperRegistry 

  * What is **MapperRegistry**? As the name suggests, mappers need to be registered in mybatis-config.xml. Else, MyBatis would not know their existence.
  * Plausible cause: Mapper.xml files not registered in the core mybatis-config.xml

* org.apache.ibatis.builder.BuilderException: Error parsing SQL Mapper Configuration. Cause: java.io.IOException: Could not find resource com/daba/dao/UserMapper.xml

  * Plausible cause: If UserMapper.xml exists but Java is saying it cannot be found, possible Maven resource filtering issue. A good way to check is to check if the corresponding Mapper.xml files exists in the target folder. Fixed by configuring Maven resources in pom.xml files as followed:

    ```xml
    <build>
            <resources>
                <resource>
                    <directory>src/main/resources</directory>
                    <includes>
                        <include>**/*.properties</include>
                        <include>**/*.xml</include>
                    </includes>
                    <filtering>true</filtering>
                </resource>
                <resource>
                    <directory>src/main/java</directory>
                    <includes>
                        <include>**/*.properties</include>
                        <include>**/*.xml</include>
                    </includes>
                </resource>
            </resources>
        </build>
    ```

    ^ Note that while adding this to the root level pom.xml often works, it is safer to add it to every package's pom.xml where Mapper.xml files are present.

* Other common mistakes:

  * Fail to register xml files correctly.
  * Fail to bind interfaces correctly.
  * Wrong method or method names.
  * Wrong return type.
  * Maven related issues.

# 2b. More things to know about MyBatis

## 2b.1 Four core interfaces/classes of MyBatis (mybatis.org)

* SqlSessionFactoryBuilder

  This class can be instantiated, used and thrown away. There is no need to keep it around once you've created your SqlSessionFactory. Therefore the best scope for instances of SqlSessionFactoryBuilder is method scope (i.e. a local method variable). You can reuse the SqlSessionFactoryBuilder to build multiple SqlSessionFactory instances, but it's still best not to keep it around to ensure that all of the XML parsing resources are freed up for more important things.

* SqlSessionFactory

  Once created, the SqlSessionFactory should exist for the duration of your application execution. There should be little or no reason to ever dispose of it or recreate it. It's a best practice to not rebuild the SqlSessionFactory multiple times in an application run. Doing so should be considered a “bad smell”. Therefore the best scope of SqlSessionFactory is application scope. This can be achieved a number of ways. The simplest is to use a Singleton pattern or Static Singleton pattern.

* SqlSession

  Each thread should have its own instance of SqlSession. Instances of SqlSession are not to be shared and are not thread safe. Therefore the best scope is request or method scope. Never keep references to a SqlSession instance in a static field or even an instance field of a class. Never keep references to a SqlSession in any sort of managed scope, such as HttpSession of the Servlet framework. If you're using a web framework of any sort, consider the SqlSession to follow a similar scope to that of an HTTP request. In other words, upon receiving an HTTP request, you can open a SqlSession, then upon returning the response, you can close it. Closing the session is very important. You should always ensure that it's closed within a finally block. The following is the standard pattern for ensuring that SqlSessions are closed:

  ```java
  try (SqlSession session = sqlSessionFactory.openSession()) {
    // do work
  }
  ```

  Using this pattern consistently throughout your code will ensure that all database resources are properly closed.

  `Note:` In our case, since we wrapped openSession() in an util class, the try-with-resource should then instead be around the reference to the class method getSqlSession. Otherwise, the session would be out of scope before we actually use it.

* Mapper Instances

  Mappers are interfaces that you create to bind to your mapped statements. Instances of the mapper interfaces are acquired from the SqlSession. As such, technically the broadest scope of any mapper instance is the same as the SqlSession from which they were requested. However, the best scope for mapper instances is method scope. That is, they should be requested within the method that they are used, and then be discarded. They do not need to be closed explicitly. While it's not a problem to keep them around throughout a request, similar to the SqlSession, you might find that managing too many resources at this level will quickly get out of hand. Keep it simple, keep Mappers in the method scope. The following example demonstrates this practice:

  ```Java
  try (SqlSession session = sqlSessionFactory.openSession()) {
    BlogMapper mapper = session.getMapper(BlogMapper.class);
    // do work
  }
  ```


# 3. CRUD

## 3.1. Namespace

The namespace field in Mapper.xml needs to be consistent with the name of the mapper interface.

## 3.2. Read (XML select tag)

```xml
<!--namespace: target DAO/Mapper interface-->
<mapper namespace="com.daba.dao.UserMapper">
    <!-- id is the name of the method. resultType is the generic type of the returning collection -->
    <select id="getUserList" resultType="com.daba.pojo.User">
        SELECT * FROM mybatis.user;
    </select>
	
    <!-- In this case parameter type can be omitted since it is a native type -->
    <select id="getUserListById" parameterType="int" resultType="com.daba.pojo.User">
        SELECT * FROM mybatis.user where id=#{id};
    </select>
</mapper>
```



* id: The name of the retrival method in the namespace.
* resultType: The generic return type of the query.

* parameterType: The types of the parameters of the method. Use #{paramterName} in SQL to dicate which parameter to use.

## 3.3. Create (XML insert tag)

In addition to the above XML snippet: 

```XML
<!-- Notice that the "insertUser" method takes a parameter of type User -->
<!-- Essentially MyBatis is flatting the fields of Objects into parameters -->
<!-- It looks like the key related stuff do nothing. They are just there 
            in case needed in the future -->
<insert id="insertUser" parameterType="com.daba.pojo.User">
    INSERT INTO mybatis.user (name, pwd) VALUES (#{name}, #{pwd})
</insert>
```

* It is important to remember that any CUD operations need to be committed. Unlike JDBC, MyBatis does not auto-commit by default.

## 3.4. Update (XML update tag)

```XML
<update id="updateUser" parameterType="com.daba.pojo.User">
    UPDATE mybatis.user SET name=#{name}, pwd=#{pwd} WHERE id = #{id};
</update>

```

## 3.5. DELETE (XML delete tag)

```XML
<delete id="deleteUser" parameterType="int">
    DELETE FROM mybatis.user WHERE id=#{id};
</delete>
```

## 3.6. Fuzzy Query (Using "like" and wildcards)

How the mapper interface method is defined: 

```java
// Fuzzy Query
List<User> getUsersLike(String s);
```

Add following tag in Mapper.xml:

```xml
<!-- Note that no parameter is specified. Because only the matcher expr is needed -->
<!-- An alternative way to do this is use wildcards here instead of in Java -->
<select id="getUsersLike" resultType="com.daba.pojo.User">
    SELECT * FROM mybatis.user WHERE name like #{expr};
</select>
```

How to use the mapper:

```java
@Test
public void getUsersLike(){
    try(SqlSession sqlSession = MybatisUtils.getSqlSession()){
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        // Keep in mind that SQL is case insensitive
        List<User> usersLike = mapper.getUsersLike("%M%"); 
        System.out.println(usersLike);
    }
}
```

* When dealing with fuzzy query...`BEWARE OF SQL INJECTION!!!`

## 3.7. Epilogue

### 3.7.1  Other ways for parameter mapping

For example, using paramaterType="map" i.e. using a Map collection for value-parameter mapping. Where the keys are the parameters in #{...} and values are their corresponding values. This is useful in scenarios like when you have lots of parameters. 

# 4. MyBatis Configuration XML

``The next few sections are discussed around Module mybatis-02`` 

## 4.1 Core Configurations (mybatis.org)

* mybatis-config.xml (Any name is fine. This is officially recommended)

  * The MyBatis configuration contains settings and properties that have a dramatic effect on how MyBatis behaves. The high level structure of the document is as follows:

  ```xml
  configuration
      properties
      settings
      typeAliases
      typeHandlers
      objectFactory
      plugins
      environments
      environment
      transactionManager
      dataSource
      databaseIdProvider
      mappers
  ```

  