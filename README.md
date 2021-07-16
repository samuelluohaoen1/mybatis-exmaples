# 1. Introduction

In plain words, the goal of MyBatis is not to do fancy things, but to simply make the life of developers easier. Without MyBatis, Java developers had to use JDBC where they have to deal with trivial DB connectivity related logics. MyBatis now gets rid of the need of worrying about DB connectivity logics, thus, increases code readability and scalability.  Do note that anything you can achieve with MyBatis, you can achieve the same without MyBatis.

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

* SqlSessionFactoryBuilder (Being fed "mybatis-config.xml" to produce the desired factory)

  This class can be instantiated, used and thrown away. There is no need to keep it around once you've created your SqlSessionFactory. Therefore **the best scope for instances of SqlSessionFactoryBuilder is method scope** (i.e. a local method variable). You can reuse the SqlSessionFactoryBuilder to build multiple SqlSessionFactory instances, but it's still best not to keep it around to ensure that all of the XML parsing resources are freed up for more important things.

* SqlSessionFactory (Being fed the "Mapper.xml" files to produce the desired SqlSession)

  Recall the idea of a SQL Connection Pool from JDBC. SqlSessionFactory works in a similar way.

  Once created, the SqlSessionFactory should exist for the duration of your application execution. There should be little or no reason to ever dispose of it or recreate it. It's a best practice to not rebuild the SqlSessionFactory multiple times in an application run. Doing so should be considered a “bad smell”. Therefore **the best scope of SqlSessionFactory is application scope**. This can be achieved a number of ways. The simplest is to use a **Singleton pattern or Static Singleton pattern**.

* SqlSession

  Each thread should have its own instance of SqlSession. Instances of SqlSession are not to be shared and are **not thread safe**. Therefore **the best scope is request or method scope**. Never keep references to a SqlSession instance in a static field or even an instance field of a class. Never keep references to a SqlSession in any sort of managed scope, such as HttpSession of the Servlet framework. If you're using a web framework of any sort, consider the SqlSession to follow a similar scope to that of an HTTP request. In other words, upon receiving an HTTP request, you can open a SqlSession, then upon returning the response, you can close it. Closing the session is very important. You should always ensure that it's closed within a finally block. The following is the standard pattern for ensuring that SqlSessions are closed:

  ```java
  try (SqlSession session = sqlSessionFactory.openSession()) {
    // do work
  }
  ```

  Using this pattern consistently throughout your code will ensure that all database resources are properly closed.

  `Note:` In our case, since we wrapped openSession() in an util class, the try-with-resource should then instead be around the reference to the class method getSqlSession. Otherwise, the session would be out of scope before we actually use it.

* Mapper Instances

  Mappers are interfaces that you create to bind to your mapped statements. Instances of the mapper interfaces are acquired from the SqlSession. As such, technically the broadest scope of any mapper instance is the same as the SqlSession from which they were requested. However, **the best scope for mapper instances is method scope**. That is, they should be requested within the method that they are used, and then be discarded. They do not need to be closed explicitly. While it's not a problem to keep them around throughout a request, similar to the SqlSession, you might find that managing too many resources at this level will quickly get out of hand. Keep it simple, keep Mappers in the method scope. The following example demonstrates this practice:

  ```Java
  try (SqlSession session = sqlSessionFactory.openSession()) {
    BlogMapper mapper = session.getMapper(BlogMapper.class);
    // do work
  }
  ```

  `Note:` Failing to adhere to the lifetime & scope standards when dealing with the above objects can lead to serious concurrency issues.

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

## 3.5. Delete (XML delete tag)

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

`We have created a minimal MyBatis project. The following sections discuss how to improve and optimize!`

# 4. MyBatis Configuration XML

``The next few sections are discussed around Module mybatis-02`` 

## 4.1. Core Configurations (mybatis.org)

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

* We can configure multiple environments to satisfy our development need. We only need to change the default environment to use.

## 4.2. Configure mybatis-config.xml by Java Properties

In `db.properties`:

```properties
driver=com.mysql.cj.jdbc.Driver
url=jdbc:mysql://localhost:3306/mybatis?useSSL=true&useUnicode=true&characterEncoding=UTF-8
username=root
password=13579lanzhousB!
```

Reference in `mybatis-config.xml`:

```xml
<!-- Reference external Properties file -->
<properties resource="db.properties">
    <!-- We can also add new properties to be referenced below -->
    <property name="env" value="development"/>
</properties>

<environments default="${env}">
    <environment id="development">
        <transactionManager type="JDBC"/>
        <dataSource type="POOLED">
            <property name="driver" value="${driver}"/>
            <property name="url" value="${url}"/>
            <property name="username" value="${username}"/>
            <property name="password" value="${password}"/>
        </dataSource>
    </environment>
</environments>
```

* Note that if two properties have the same name with one in the external properties file and the other one in the internal properties tag, the external one takes priority.

## 4.3. Type Aliases

* Method 1: Explicit naming

  In `mybatis-config.xml`:

  ```xml
  <typeAliases>
      <typeAlias type="com.daba.pojo.User" alias="User"/>
  </typeAliases>
  ```

* Method 2: Scanning a package

  In `mybatis-config.xml`:

  ```xml
  <!-- MyBatis automatically searches for Java classes as beans. The default name
          for these beans are the lowercase letters of the class name so that you
          can distinguish the auto detected ones from explicit ones-->
  <typeAliases>
      <package name="com.daba.pojo"/>
  </typeAliases>
  ```

  * If this method is used, one can annotate above the class definition to customize alias name:

    ```java
    import org.apache.ibatis.type.Alias;
    
    @Alias("customUser")
    public class User{
        ...
    }
    ```

## 4.4. Important Settings for the Settings tag

* lazyLoadingEnabled
* cacheEnabled
* mapUnderscoreToCamelCase
* logImpl
* More on MyBatis documentation ...

## 4.5. Other configs

* typeHandlers
* objectFactory
* plugins
  * MyBatis Generator Core
  * MyBatis Plus
  * Mappers ...

## 4.6. Mappers

Register Mappers into MapperRegistry:

* Method 1 (Recommended) - Our previous way, using resource attribute:

  ```xml
  <mappers>
  	<mapper resource="com/daba/dao/UserMapper.xml"/>    
  </mappers>
  ```

* Method 2 - Using fully qualified class name: 

  ```xml
  <mappers>
      <mapper class="com.daba.dao.UserMapper"/>
  </mappers>
  ```

  * Note 1: Interface and its Mapper config XML file must have the same name (other than the extension).
  * Note 2: Interface and its Mapper config XML file must be in the same package.

* Method 3 - Package scanning (Using to scanning seen in Alias) :

  ```XML
  <mappers>
      <package name="com.daba.dao"/>
  </mappers>
  ```

  * Note 1: Interface and its Mapper config XML file must have the same name (other than the extension).
  * Note 2: Interface and its Mapper config XML file must be in the same package.

# 5. Different column name and property name

``The next few sections are discussed around Module mybatis-03``

Suppose we rename "pwd" to "password" in our `User` class:

```java
public class User {
    private int id;
    private String name;
    private String password;
}
```

Running the following test:

```java
@Test
public void getUserList(){
    try(SqlSession sqlSession = MybatisUtils.getSqlSession()){
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        User user = mapper.getUserById(1); // Keep in mind that SQL is case insensitive
        System.out.println(user);
    }
}
```

Output:

```txt
User{id=1, name='Samuel', pwd='null'}
```

This is because the field name does not match the variable name in DB. The typeHandler cannot map between them.

## 5.1. Solution 1 - Alias SQL vars in Mapper XML beans

```xml
<select id="getUserById" parameterType="int" resultType="User">
    SELECT id, name, pwd as password FROM mybatis.user where id=#{id};
</select>
```

## 5.2. Solution 2 - resultMap tag

```xml
<mapper namespace="com.daba.dao.UserMapper">
    <resultMap id="UserMap" type="User">
<!-- Since the following fields have the same name, they are already correctly mapped,
       We do not need to specify their mapping. -->
<!--        <result column="id" property="id"/>-->
<!--        <result column="name" property="name"/>-->
        <result column="pwd" property="password"/>
    </resultMap>

    <select id="getUserById" parameterType="int" resultMap="UserMap">
        SELECT * FROM mybatis.user where id=#{id};
    </select>
</mapper>
```

# 6. Log

``The next few sections are discussed around Module mybatis-04``

## 6.1. LogFactory

Debugging SQL operations:

Before: sout,debugger.

MyBatis: LogFactory.

Some available modes:

* SLF4J
* LOG4J -- A must
* LOG4J2
* JDK_LOGGING
* COMMONS_LOGGING
* STDOUT_LOGGING -- A must
* NO_LOGGING

Which one to use to be specified in the settings tag in mybatis-config.xml.

```xml
<properties resource="db.properties">
    <property name="env" value="development"/>
</properties>

<settings>
    <setting name="logImpl" value="STDOUT_LOGGING"/>
</settings>

<typeAliases>
    <typeAlias type="com.daba.pojo.User" alias="User"/>
</typeAliases>
```

## 6.2. Using LOG4J

* New pom.xml dependency

* `log4j.properties`:

  ```properties
  # Output DEBUG level logs to console and file, console and file will be defined in the properties below
  log4j.rootLogger = DEBUG, console, file
  
  # Console output related properties
  log4j.appender.console = org.apache.log4j.ConsoleAppender
  log4j.appender.console.Target = System.out
  log4j.appender.console.Threshold = DEBUG
  log4j.appender.console.layout = org.apache.log4j.PatternLayout
  log4j.appender.console.layout.ConversionPattern=[%c]-%m%n
  
  # File output related properties
  log4j.appender.file = org.apache.log4j.RollingFileAppender
  log4j.appender.file.File = ./log/daba.log
  log4j.appender.file.MaxFileSize = 10mb
  log4j.appender.file.Threshold = DEBUG
  log4j.appender.file.layout = org.apache.log4j.PatternLayout
  log4j.appender.file.layout.ConversionPattern = [%p][%d{yy-MM-dd}][%c]%m%n
  
  # Log levels
  log4j.logger.org.mybatis=DEBUG
  log4j.logger.java.sql=DEBUG
  log4j.logger.java.sql.Statement=DEBUG
  log4j.logger.java.sql.ResultSet=DEBUG
  log4j.logger.java.sql.PreparedStatement=DEBUG
  ```

* import org.apache.log4j.Logger where you want to use Log4j

* ```java
  static Logger logger = Logger.getLogger(UserMapperTest.class);
  ```

* ```java
  @Test
  public void testLog4j(){
      logger.info("info: Executing testLog4j method.");
      logger.debug("debug: Executing testLog4j method.");
      logger.error("error: Executing testLog4j method.");
  }
  ```

# 7. Annotations

``The next few sections are discussed around Module mybatis-05``

To map DAO methods to beans, one can use annotations.

In `mybatis-config.xml`, the mappers tag now look like this:

```xml
<mappers>
    <mapper class="com.daba.dao.UserMapper"/>
</mappers>
```

In `UserMapper.java`, annotate like this:

```JAVA
public interface UserMapper {
    @Select("SELECT id, name, pwd as password FROM mybatis.user;")
    List<User> getUsers();
}

// Now that we do not have a Mapper XML, we specify parameters used in SQL
// using the @Param annotation
@Select("SELECT id, name, pwd as password FROM mybatis.user WHERE id=#{id};")
User getUserById(@Param("id") int id);

// Like the example for previous modules, if the parameter is a reference,
// no need to use @Param
@Update("UPDATE mybatis.user SET name=#{name}, pwd=#{password} WHERE id = #{id};")
int updateUserById(User user);
```

Note that because now we are using annotations, we cannot use result mapping now. But we can still map property names to column names using aliases.

**About @Param() annotation:**

* Java native types need to have @Param() annotations when being used, including String.
* Custom types do not need it.
* If there is only one native type, do not need it. But recommended for clarity and good habit.
* The parameters used in SQL statements should have matching names with names in @Param()
* Difference between #{} and ${}: 
  * #{} attempts to prevent SQL injection while ${} does not.
  * Therefore, #{} is recommended.

# 8. ResultMap: Association & Collection

``The next few sections are discussed around Module mybatis-06``

To prepare for this section, create a new table in mybatis DB.

```mysql
CREATE TABLE `teacher` (
  `id` INT NOT NULL,
  `name` VARCHAR(30) DEFAULT NULL,
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8

INSERT INTO teacher(`id`, `name`) VALUES (1, 'Alex'); 

CREATE TABLE `student` (
  `id` INT NOT NULL,
  `name` VARCHAR(30) DEFAULT NULL,
  `tid` INT DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fktid` (`tid`),
  CONSTRAINT `fktid` FOREIGN KEY (`tid`) REFERENCES `teacher` (`id`)
) DEFAULT CHARSET=utf8

INSERT INTO `student` (`id`, `name`, `tid`) VALUES ('1', 'Daba', '1'); 
INSERT INTO `student` (`id`, `name`, `tid`) VALUES ('2', 'Joanna', '1'); 
INSERT INTO `student` (`id`, `name`, `tid`) VALUES ('3', 'Jason', '1'); 
INSERT INTO `student` (`id`, `name`, `tid`) VALUES ('4', 'Mark', '1'); 
INSERT INTO `student` (`id`, `name`, `tid`) VALUES ('5', 'Twenty', '1');
```



Association: Many similar entities associated with another entity.

Collection: A single entity has a collection a many similar entities.

## 8.1. Association Query: Many to one relationship

`note`: When filling in child tags of resultMap tag, explicit add one child tag for each property needed.

When each query result should contain objects of another class, special treatments are needed. In general,  if many of our query results share a common reference to another object, it is a association relationship.

In `StudentMapper.xml`:

```xml
<!-- Need to handle complicated Properties separately
     Objects: association tag
     Collections: collection tag
     Here, each teacher refers should to the same teacher object
    -->
<!-- Method 1: Sub-query -->
<select id="getStudent" resultMap="StudentTeacher">
    SELECT * FROM student;
</select>

<resultMap id="StudentTeacher" type="Student">
    <result property="id" column="id"/>
    <result property="name" column="name"/>
    <association property="teacher" column="tid" javaType="Teacher" select="getTeacher"/>
</resultMap>

<select id="getTeacher" resultType="Teacher">
    SELECT * FROM teacher WHERE id=#{tid};
</select>

<!--  Method 2: nesting result  -->
<select id="getStudent2" resultMap="StudentTeacher2">
    SELECT s.id sid, s.name sname, t.name tname
    FROM student s, teacher t
    WHERE s.tid = t.id;
</select>

<resultMap id="StudentTeacher2" type="Student">
    <result property="id" column="sid"/>
    <result property="name" column="sname"/>
    <association property="teacher" javaType="Teacher">
        <result property="name" column="tname"/>
    </association>
</resultMap>
```

Plainly speaking, in **method 1**, when we see that one property is actually another class, we can define another tag to tell MyBatis how to retrieve what we want. Thus, Sub-query.

In **method 2**, we write a jumbo union query and tell MyBatis what to expect as result. See that method 2 results in less tags which could be a big advantage.



## 8.2. Collection Query: One to Many relationship

``The next few sections are discussed around Module mybatis-07``

Using the student-teacher example in 8.1, this relationship can be viewed as: One teacher has a collection of students.

Very similar to mybatis-06, just look into the mapper files and java classes.

# 9. Dynamic SQL

Dynamic SQL is rather intuitively. Therefore, no full blown implemented module is demonstrated.

To use Dynamic SQL:

* Use map to pass parameters to your query i.e. use the attribute `paramaterType="map"` when writing the mapper bean.
* Apply Dynamic SQL logic in query code. The Syntax is similar to JSTL.
* One can also combine Dynamic SQL with fuzzy query. The combination of the two is really robust.

## 9.1. `if` tag

`Suppose that we are now dealing with the following schema:`

```xml
CREATE TABLE `blog`(
`id` VARCHAR(50) NOT NULL COMMENT 'blog id',
`title` VARCHAR(100) NOT NULL COMMENT 'blog title',
`author` VARCHAR(30) NOT NULL COMMENT 'blog author',
`create_time` DATETIME NOT NULL COMMENT 'creation time',
`views` INT(30) NOT NULL COMMENT 'number of views'
)DEFAULT CHARSET=utf8;
```

**First thing to note is that** we have a column named `create_time`. This is a good time to use the `mapUnderscoreToCamelCase` setting (assuming you want to adhere your Java code to standard).

In a typical `BlogMapper.xml`:

```xml
<!--  a select bean  -->
<select id="queryBlogIf" parameterType="map" resultType="blog">
	SELECT * FROM blog WHERE 1=1  # <----- take a mental note at this awkward WHERE
    <if test= "title != null">
    	AND title=#{title}
    </if>
    <if test= "author != null">
    	AND author=#{author}
    </if>
	;
</select>
```

Hopefully it is clear what is going on from the above example.

## 9.2. `where` tag

To resolve the awkward WHERE issue in 9.1. The `where` tag is introducted. 

It inserts a SQL `WHERE` automatically if and only if at least one logic tag below is true.

`where` tag will also trim the dangling `AND`, `OR` smartly.

Therefore, the same exmaple from 9.1 can be rewritten as:

```xml
<!--  a select bean  -->
<select id="queryBlogIf" parameterType="map" resultType="blog">
	SELECT * FROM blog
    <where>
        <if test= "title != null">
            AND title=#{title}
        </if>
        <if test= "author != null">
            AND author=#{author}
        </if>
    </where>
	;
</select>
```



## 9.3. `choose`, `when`, `otherwise` tags

When used together `choose`, `when`, `otherwise` behaves very much like switch statements.

In a typical `BlogMapper.xml`:

```xml
<select id="queryBlogChoose" parameterType="map" resultType="blog">
	SELECT * FROM blog
    <where>
    	<choose>
        	<when test="title != null">
            	title=#{title}
            </when>
            <when test='author != null'>
            	and author=#{author}
            </when>
            <otherwise>
            	and views=#{views}
            </otherwise>
        </choose>
    </where>
    ;
</select>
```

`Note` that similar to a boolean "or", if one when is true, the rest have been short-circuited.

## 9.4.  `set` tag

Similar to `where`, `set` is a useful tag when working with SQL `update`

It will dynamically add SQL `set` and trim "," (commas) smartly.

In a typical `BlogMapper.xml`:

```XML
<update id="updateBlog" parameterType="map">
	UPDATE BLOG
    <set>
    	<if test="title != null">
        	title = #{title},
        </if>
        <if test="author != null">
        	author = #{author}
        </if>
    </set>
    WHERE id=#{id};
</update>
```

`Note` that when #{id} parameter should also be stored in the paramter map in the case. Because where else could you put it?

Also, unlike `where`, it wouldn't make sense for all the `if` tags to evaluate to false. Because we need to update at least one column.

## 9.5. `trim` tag

`set` and `where` are basically impletations of trim. You can customize behaviors similar to `set` and `where` using trim to make your logic even more robust.

## 9.6. `sql` tag

Save SQL snippets as beans in Mapper XML for reuse.

In a typical `BlogMapper.xml`:

```xml
<!--  definition  -->
<sql id="if-title-author">
	<if test="title != null">
    	title = #{title}
    </if>
    <if test="author != null">
    	and author = #{author}
    </if>
</sql>

<!--  usage  -->
<select id="queryBlogIf" parameterType="map" resultType="blog">
	select * from blog
    <where>
    	<include refid="if-title-author"/>
    </where>
    ;
</select>
```

`Note`:  Not good for complicated SQL logic. For example, if multiple tables are involved, it would be hard to keep the alias names consistent. Also, do not put tags like `where` in the snippets because they are way too case specific.

## 9.7. `foreach` tag

Suppose we want to implement the following SQL:

```mysql
SELECT * FROM blog WHERE 1=1 AND (id=1 OR id=2 OR id=3);
```

If we brute force, we need to give all 3 `id`s an alias. Apparently this gets absurd as we have more `id`s.

Therefore,

In a typical `BlogMapper.xml`:

```xml
<select id="queryBlogForeach" parameterType="map" resultType="blog">
	select * from blog
    
    <where>
    	<foreach collection="ids" item="id" open="AND (" close=")" separator="OR">
        	id = #{id}
        </foreach>
    </where>
    ;
</select>
```

How to provide the collection needed for `foreach`:

In a typical `service class`:

```java
/* Note that we want a <Object, Object> or <String, Object> because
   parameters can really be any class. */
HashMap map = new HashMap();	// Another demonstration of Map's robustness
ArrayList<Integer> ids = new ArrayList<Integer>();
ids.add(1);
ids.add(2);
map.put("ids", ids);

List<Blog> blogs = mapper.queryBlogForeach(map);
```

# 10. Cache

``The next few sections are discussed around Module mybatis-08``

**Some Data Server key concepts to know**

* Master-Slave Replication
* Read/Write Splitting
* Cache servers and actual data servers

**Why caching?**

	* It is costly to connect to SQL server, run transaction, commit, disconnect from server even with pooled technology.
	* Cache the result of query in RAM to optimize.
	* When we lookup similar data, we can simply retrieve it from cache. Greatly improves the performance of highly concurrent environments.

**What kind of data are good candidate for caching?**

* Data that are often read but rarely changed.

# 10.1. MyBatis Cache

* Level 1 Cache: `SqlSession` level cache. Accessed directly when reading. This is enabled by default (plus I don't think we can disable it).
* Level 2 Cache: namespace level cache. Must be access explicitly and can be customized by implementing interfaces provided by MyBatis.

**When is cached cleared?**

* Cache is useless when we are reading different entries.
* Cache is also useless when we use sqlSession of another Mapper XML.

* Level 1 Cache is cleared whenever a CUD operation happens. However, we can explictly stop a CUP operation from clearing cache by using `flushCache"false"` attribute.

  ```xml
  <update id="updateUser" parameterType="user" flushCache="false">
  	...
  </update>
  ```

   

* MyBatis also clears old cache by using either LRU(default) or FIFO.

* We can also clear cache manually using `sqlSession.clearCache()`.

* `Note` that when a transaction is done (the lifetime of a `SqlSession` object), we lose our level 1 Cache. This is pretty useless. **We want cache that persists among users where one user can use the cache from other users**.

## 10.2. Level 2 Cache

Level 2 Cache is also called Global Cache (But remember,  it is namespace level). It exists because level 1 cache is nearly worthless.

To use level 2 cache, simply add `cache` tag in Mapper XML.

There is a setting called `cacheEnabled` which is enabled by default. But just in case it is recommended to set it to true explicitly.

In `UserMapper.xml`:

```xml
<mapper namespace="com.daba.dao.UserMapper">
	
    <!--  This tag enables cache  -->
    <cache/>
    
    <!-- We can also config some options -->
    <!--
    <cache eviction="LRU" flushInterval="60000" size="512"  readOnly="true"/>
	-->

    <select id="selectUserById" parameterType="_int" resultType="User">
        SELECT * FROM user WHERE id=#{id};
    </select>
</mapper>
```

**How it works**:  

* When a query is made, the result is stored in a Level 1 cache.
* When a session closes, its level 1 cache is also cleared. However, level 1 cache's data will be saved into level 2 cahe.
* Now when a new session in the same namespace is created, that new session will automatically use level 2 cache.

**Note**:

* If readOnly attribute is "false", the Java object being cached must implement the java.io.Serializable interface! Else, you get:

  ```txt
  org.apache.ibatis.cache.CacheException: Error serializing object.
  ```



**This concludes most useful things to know about MyBatis.**

