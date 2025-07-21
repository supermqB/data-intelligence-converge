package com.lrhealth.data.converge.common.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author jinmengyu
 * @date 2024-11-11
 */
public class ConnectionWrapper {

    private final Connection connection;
    private long lastAccessTime;

    public ConnectionWrapper(Connection connection) {
        this.connection = connection;
        this.lastAccessTime = System.currentTimeMillis();
    }

    public Connection getConnection() {
        return connection;
    }

    public void updateLastAccessTime() {
        this.lastAccessTime = System.currentTimeMillis();
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
