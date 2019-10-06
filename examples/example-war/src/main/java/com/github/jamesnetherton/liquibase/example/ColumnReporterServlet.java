package com.github.jamesnetherton.liquibase.example;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

@WebServlet(name = "ColumnReporterServlet", urlPatterns = { "/*" }, loadOnStartup = 1)
public class ColumnReporterServlet extends HttpServlet {

    private static final String QUERY = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'PERSON' ORDER BY COLUMN_NAME ASC";

    @Resource(lookup = "java:jboss/datasources/ExampleDS")
    DataSource dataSource;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try(Connection connection = dataSource.getConnection()) {
            try(Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery(QUERY);
                while (resultSet.next()) {
                    response.getOutputStream().println("======> " + resultSet.getString("COLUMN_NAME").toLowerCase());
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
