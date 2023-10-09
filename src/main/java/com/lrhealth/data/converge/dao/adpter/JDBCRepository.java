package com.lrhealth.data.converge.dao.adpter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.teasoft.honey.osql.core.BeeFactory;
import org.teasoft.honey.osql.core.ExceptionHelper;
import org.teasoft.honey.osql.core.HoneyUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author jinmengyu
 * @date 2023-10-08
 */
@Slf4j
@Repository
public class JDBCRepository {

    public String execSql(String sql){
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            BeeFactory instance = BeeFactory.getInstance();
            instance.getDataSource().getConnection();
            conn = instance.getDataSource().getConnection();
            String execSql = HoneyUtil.deleteLastSemicolon(sql);
            pst = conn.prepareStatement(execSql);
            rs = pst.executeQuery();
            while (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            throw ExceptionHelper.convert(e);
        }finally {
            if(rs!=null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if(pst!=null) {
                try {
                    pst.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if(conn!=null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
