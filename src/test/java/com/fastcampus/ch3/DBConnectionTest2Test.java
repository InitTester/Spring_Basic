package com.fastcampus.ch3;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.jta.SpringJtaSynchronizationAdapter;

import javax.sql.DataSource;

import java.sql.*;
import java.util.Date;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/spring/**/root-context.xml"})
public class DBConnectionTest2Test {

    @Autowired DataSource ds;

    @Test
    public void insertUserTest() throws Exception{
        User user = new User("asdfdw","1111","abc","aaa@asdf.com",new Date(),"fb", new Date());
        deleteAll();
        int rowCnt = insertUser(user);

        assertTrue(rowCnt==1);
    }

    @Test
    public void selectUserTest() throws Exception {
        deleteAll();
        User user = new User("asdfdw","1111","abc","aaa@asdf.com",new Date(),"fb", new Date());
        int rowCnt = insertUser(user);
        User user2 = selectUser("asdfdw");

        assertTrue(user.getId().equals("asdfdw"));
    }

    @Test
    public void deleteUserTest() throws Exception{
        deleteAll();
        int rowCnt = deleteUser("asdfdw");

        assertTrue(rowCnt==0);

        User user = new User("asdfdw","1111","abc","aaa@asdf.com",new Date(),"fb", new Date());
        rowCnt = insertUser(user);
        assertTrue(rowCnt==1);

        rowCnt = deleteUser(user.getId());
        assertTrue(rowCnt==1);

        assertTrue(selectUser(user.getId())==null);
    }


    @Test
    public void UpdateUserTest() throws Exception {
        User user = new User("asdfdw","1111","abc","aaa@asdf.com",new Date(),"fb", new Date());
        deleteAll();
        int rowCnt = insertUser(user);

        assertTrue(rowCnt==1);

         user = new User("asdfdw","222","abc","aaa@asdf.com",new Date(),"fb", new Date());
         rowCnt = updateUser(user);

        assertTrue(rowCnt==1);
    }

    // 매개변수로 받은 사용자 정보로 user_info테이블을 update하는 메서드
    public int updateUser(User user) throws Exception {

        Connection conn = ds.getConnection();

        String sql = "update user_info set name= ? , email = ?  where id = ?";

        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1,user.getName());
        pstmt.setString(2,user.getEmail());
        pstmt.setString(3,user.getId());
//        pstmt.setString(3,user.getBirth());

        int rowCnt = pstmt.executeUpdate();

        return rowCnt;
    }

    public int deleteUser(String id) throws Exception{
        Connection conn = ds.getConnection();

        String sql = "delete from user_info where id = ?";

        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1,id);
        //int rowCnt = pstmt.executeUpdate();
        return pstmt.executeUpdate();
    }

    public User selectUser(String id) throws Exception{
        Connection conn = ds.getConnection();

        String sql="select * from user_info where id=?";

        PreparedStatement pstmt = conn.prepareStatement(sql); // SQL Injection 공격, 성능향상
        pstmt.setString(1, id);
        ResultSet rs =  pstmt.executeQuery(); // select

        if(rs.next()){
            User user = new User();
            user.setId(rs.getString(1));
            user.setPwd(rs.getString(2));
            user.setName(rs.getString(3));
            user.setEmail(rs.getString(4));
            user.setBirth(new Date(rs.getDate(5).getTime()));
            user.setSns(rs.getString(6));
            user.setReg_date(new Date(rs.getDate(7).getTime()));

            return user;
        }
        return null;
    }

    private void deleteAll() throws Exception {
        Connection conn = ds.getConnection();

        String sql="delete from user_info ";

        PreparedStatement pstmt = conn.prepareStatement(sql); // SQL Injection 공격, 성능향상
        pstmt.executeUpdate();
    }

    @Test
    public void TransactionTest() throws Exception {
        Connection conn=null;

        try {
            deleteAll();
            conn = ds.getConnection();
            conn.setAutoCommit(false); // conn.setAutoCommit(true) < 기본

//        insert into user_info (id, pwd, name, email, birth, sns, reg_date)
//    value('asdf2','1234','smith','aaa@aaa.com','2020-01-01','facebook',now());

            String sql="insert into user_info value(?, ?,?,?,?,?,now())";

            PreparedStatement pstmt = conn.prepareStatement(sql); // SQL Injection 공격, 성능향상
            pstmt.setString(1, "asdf");
            pstmt.setString(2, "1234");
            pstmt.setString(3, "abc");
            pstmt.setString(4, "aad@dfe.com");
            pstmt.setDate(5,new java.sql.Date(new Date().getTime()) );
            pstmt.setString(6, "sb");

            int rowCnt = pstmt.executeUpdate();

            pstmt.setString(1,"asdf2");
            rowCnt = pstmt.executeUpdate(); // insert, update, delete

            conn.commit();

        } catch (Exception e) {
            conn.rollback();
            throw new RuntimeException(e);
        } finally {

        }


    }

    // 사용자 정보를 user_info 테이블에 저장하는 메서드
    public int insertUser(User user) throws Exception {

        Connection conn = ds.getConnection();

//        insert into user_info (id, pwd, name, email, birth, sns, reg_date)
//    value('asdf2','1234','smith','aaa@aaa.com','2020-01-01','facebook',now());

        String sql="insert into user_info value(?, ?,?,?,?,?,now())";

        PreparedStatement pstmt = conn.prepareStatement(sql); // SQL Injection 공격, 성능향상
        pstmt.setString(1, user.getId());
        pstmt.setString(2, user.getPwd());
        pstmt.setString(3, user.getName());
        pstmt.setString(4, user.getEmail());
        pstmt.setDate(5,new java.sql.Date(user.getBirth().getTime()) );
        pstmt.setString(6, user.getSns());

        int rowCnt = pstmt.executeUpdate();

        return rowCnt;
    }
    @Test
    public void springJdbcConnextionTest() throws Exception{

//        ApplicationContext ac = new GenericXmlApplicationContext("file:src/main/webapp/WEB-INF/spring/**/root-context.xml");
//        DataSource ds = ac.getBean(DataSource.class);

        Connection conn = ds.getConnection(); // 데이터베이스의 연결을 얻는다.

        System.out.println("conn = " + conn);
        assertTrue(conn!=null); // 괄호 안의 조건식이 true면, 테스트 성공, 아니면 실패
    }

}