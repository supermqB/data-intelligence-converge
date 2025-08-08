package com.lrhealth.data.converge.common.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import com.lrhealth.data.converge.dao.entity.ConvDsConfig;

/**
 * @author jinmengyu
 * @date 2024-11-11
 */
@Component
public class DbConnectionManager {
    private final ConcurrentMap<String, ConnectionWrapper> connectionMap = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public DbConnectionManager() {
        // 定时任务，每分钟检查并关闭闲置连接
        scheduler.scheduleAtFixedRate(this::closeIdleConnections, 1, 1, TimeUnit.MINUTES);
    }

    public Connection getConnection(DbConnection config) {
        ConnectionWrapper wrapper = connectionMap.computeIfAbsent(config.getDbUrl(), key -> {
            try {
                DataSource dataSource = getDataSource(config);
                Connection connection = dataSource.getConnection();
                return new ConnectionWrapper(connection);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        wrapper.updateLastAccessTime();
        return wrapper.getConnection();
    }

    public Connection getConnection(ConvDsConfig ds) {
        DbConnection connection = DbConnection.builder()
                .dbUrl(ds.getDsUrl())
                .dbUserName(ds.getDsUsername())
                .dbPassword(ds.getDsPwd())
                .dbDriver(ds.getDsDriverName())
                .build();
        return this.getConnection(connection);
    }

    private DataSource getDataSource(DbConnection config) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(config.getDbUrl());
        dataSource.setUsername(config.getDbUserName());
        dataSource.setPassword(config.getDbPassword());
        dataSource.setDriverClassName(config.getDbDriver());
        return dataSource;
    }

    private void closeIdleConnections() {
        long currentTime = System.currentTimeMillis();
        connectionMap.forEach((key, wrapper) -> {
            if (currentTime - wrapper.getLastAccessTime() > TimeUnit.MINUTES.toMillis(10)) {
                wrapper.closeConnection();
                connectionMap.remove(key);
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        closeIdleConnections(); // 关闭所有连接
    }
}
