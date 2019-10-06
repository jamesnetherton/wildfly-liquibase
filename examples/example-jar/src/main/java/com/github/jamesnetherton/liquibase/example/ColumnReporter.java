package com.github.jamesnetherton.liquibase.example;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.sql.DataSource;

@Startup
@Singleton
public class ColumnReporter {

    private static final String QUERY = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'PERSON' ORDER BY COLUMN_NAME ASC";

    @Resource(lookup = "java:jboss/datasources/ExampleDS")
    DataSource dataSource;

    @PostConstruct
    public void postConstruct() {
        try(Connection connection = dataSource.getConnection()) {
            try(Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery(QUERY);
                while (resultSet.next()) {
                    System.out.println("======> " + resultSet.getString("COLUMN_NAME").toLowerCase());
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
