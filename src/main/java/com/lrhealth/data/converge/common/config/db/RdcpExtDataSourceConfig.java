package com.lrhealth.data.converge.common.config.db;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisXMLLanguageDriver;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.lrhealth.data.converge.scheduled.mybatis.handler.MybatisFillHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * 数据源及mybatis配置：rdcp-ext库
 *
 * @author lr
 */
@Slf4j
@Configuration
@MapperScan(basePackages = {"com.lrhealth.data.model.*.mapper", "com.lrhealth.data.converge.dao.mapper",
"com.lrhealth.data.converge.scheduled.dao.mapper"},
        sqlSessionFactoryRef = "rdcpExtSqlSessionFactory")
public class RdcpExtDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.rdcp-ext")
    public DataSource rdcpExtDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public SqlSessionFactory rdcpExtSqlSessionFactory(@Qualifier("rdcpExtDataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath*:/mapper/*.xml"));
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setDefaultScriptingLanguage(MybatisXMLLanguageDriver.class);
        configuration.setJdbcTypeForNull(JdbcType.NULL);
        sqlSessionFactoryBean.setConfiguration(configuration);
        sqlSessionFactoryBean.setTransactionFactory(new SpringManagedTransactionFactory());
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setMetaObjectHandler(new MybatisFillHandler());
        sqlSessionFactoryBean.setGlobalConfig(globalConfig);
        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    public PlatformTransactionManager transactionManager(@Qualifier("rdcpExtDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

}
