package com.lrhealth.data.converge.dao.adpter;

import com.lrhealth.data.converge.model.dto.DataSourceDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.teasoft.honey.osql.core.ExceptionHelper;

import java.sql.*;

/**
 * @author jinmengyu
 * @date 2023-10-08
 */
@Slf4j
@Repository
public class JDBCRepository {

    public static String execSql(String sql, DataSourceDto dataSourceDto){
        ResultSet rs = null;
        String execSql = deleteLastSemicolon(sql);
        try {
            Class.forName(dataSourceDto.getDriver());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try (Connection conn = DriverManager.getConnection(dataSourceDto.getJdbcUrl(), dataSourceDto.getUsername(), dataSourceDto.getPassword());
             PreparedStatement pst = conn.prepareStatement(execSql)){
            rs = pst.executeQuery();
            if (rs.next()) {
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
        }
        return null;
    }

    private static String deleteLastSemicolon(String sql) {
        String new_sql = sql.trim();
        if (new_sql.endsWith(";")) return new_sql.substring(0, new_sql.length() - 1); //fix oracle ORA-00911 bug.oracle用jdbc不能有分号
        return sql;
    }
}
