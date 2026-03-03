package org.example.flyora_backend;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

@SpringBootApplication
public class FlyoraBackendApplication {

    @Autowired
    private DataSource dataSource;

    public static void main(String[] args) {
        SpringApplication.run(FlyoraBackendApplication.class, args);
    }

    @PostConstruct
    public void checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String dbUrl = metaData.getURL();
            String dbName = metaData.getDatabaseProductName();
            String dbVersion = metaData.getDatabaseProductVersion();

            // In thông tin cơ sở dữ liệu ra console
            System.out.println("Connected to: " + dbName + " " + dbVersion);
            System.out.println("Database URL: " + dbUrl);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Unable to connect to the database.");
        }
    }
}
