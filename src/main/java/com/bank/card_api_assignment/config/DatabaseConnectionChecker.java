package com.bank.card_api_assignment.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@Component
public class DatabaseConnectionChecker implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionChecker.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public DatabaseConnectionChecker(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData meta = connection.getMetaData();
            // Ask the database itself which DB we are connected to
            String dbName = jdbcTemplate.queryForObject("SELECT DB_NAME()", String.class);

            log.info("=================== DATABASE CONNECTION CHECK ===================");
            log.info("Status   : CONNECTED");
            log.info("Database : {}", dbName);
            log.info("Product  : {} {}", meta.getDatabaseProductName(), meta.getDatabaseProductVersion());
            log.info("URL      : {}", meta.getURL());
            log.info("User     : {}", meta.getUserName());
            log.info("=================================================================");
        } catch (Exception e) {
            log.error("DATABASE CONNECTION FAILED: {}", e.getMessage());
        }
    }
}
