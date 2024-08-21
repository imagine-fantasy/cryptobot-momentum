package com.crypto.cmtrade.cryptobot.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Log4j2
@Configuration
public class DataSourceConfig {

    @Value("${database.jdbcUrl}")
    private String jdbcUrl;
    @Value("${database.username}")
    private String username;
    @Value("${database.password}")
    private String password;

    @Value("${database.schema}")
    private String schema;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        log.info("system url " +jdbcUrl);
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(username);
        log.info("password" +password);
        dataSource.setPassword(password);
        dataSource.setSchema(schema);
        return dataSource;
    }
}