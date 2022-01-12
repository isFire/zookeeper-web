package com.zk.config;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ResourceUtils;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

/**
 * @author <a href="mailto:zhao___li@163.com">清汤白面<a/>
 * @description
 * @date 2022-01-12 09:49
 */
@Configuration
@MapperScan(basePackages = {"com.zk.mapper"})
public class SqliteConfig {

    private static final Logger log = LoggerFactory.getLogger(SqliteConfig.class);

    private static final String USER_DIR = System.getProperty("user.dir");

    private static final String INIT_SQL = "select * from zk_data";

    @Value(value = "${sqlite.path:}")
    private String sqlitePath;

    public String getSqlitePath() {
        if (StringUtils.isBlank(this.sqlitePath)) {
            return "jdbc:sqlite:" + USER_DIR + File.separator + "zk" + File.separator + "data" + File.separator + "zk.db";
        }
        return sqlitePath;
    }


    @Bean(name = "sqliteDataSource")
    public DataSource sqliteDataSource() {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(getSqlitePath());
        dataSource.setDatabaseName("zk");
        dataSource.setEncoding(StandardCharsets.UTF_8.name());
        SQLiteConfig config = new SQLiteConfig();
        config.setEncoding(SQLiteConfig.Encoding.UTF8);
        dataSource.setConfig(config);
        try {
            initDb(dataSource.getConnection());
        } catch (SQLException e) {
            log.error("init sql error:[{}]", e.getMessage(), e);
        }
        return dataSource;
    }

    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(sqliteDataSource());
        PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = patternResolver.getResources("classpath*:mapper/*Mapper.xml");
        factoryBean.setMapperLocations(resources);
        return factoryBean.getObject();
    }

    @Bean(name = "sqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate() throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory());
    }

    void initDb(Connection connection) throws SQLException {
        boolean tableExists;
        try(PreparedStatement preparedStatement = connection.prepareStatement(INIT_SQL)) {
            preparedStatement.execute();
            tableExists = true;
        } catch (SQLException e) {
            tableExists = false;
            log.error("init zk_data table error:[{}]", e.getMessage(), e);
        }
        if (!tableExists) {
            String[] sqlList = new String[0];
            try {
                File file = ResourceUtils.getFile("classpath:sql/sqlite.sql");
                String initSQL = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                sqlList = initSQL.split(";");
            } catch (IOException e) {
                log.error("read sql content error:[{}]", e.getMessage(), e);
            }
            try {
                connection.setAutoCommit(false);
                for (String sql : sqlList) {
                    try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                        preparedStatement.execute();
                    } catch (SQLException e) {
                        log.error("prepare statement error:[{}]", e.getMessage(), e);
                    }
                }
                connection.commit();
            } catch (SQLException e) {
                log.error("commit transaction error:[{}]", e.getMessage(), e);
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    log.error("rollback error:[{}]", ex.getMessage(), e);
                }
                throw new SQLException("execute sql error", e);
            }
        }
    }


}
