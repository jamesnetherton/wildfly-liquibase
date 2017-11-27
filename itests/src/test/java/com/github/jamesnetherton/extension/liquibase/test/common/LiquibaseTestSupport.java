/*-
 * #%L
 * wildfly-liquibase-itests
 * %%
 * Copyright (C) 2017 James Netherton
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.github.jamesnetherton.extension.liquibase.test.common;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;

public class LiquibaseTestSupport {

    private static final String QUERY_TABLE_COLUMNS = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? ORDER BY COLUMN_NAME ASC";

    @ArquillianResource
    private InitialContext context;

    protected <T> T lookup(String name, Class<?> T) throws Exception {
        return (T) context.lookup(name);
    }

    protected void assertTableModified(String tableName) throws Exception {
        assertTableModified(tableName, Arrays.asList("firstname", "id", "lastname", "state", "username"));
    }

    protected void assertTableModified(String tableName, List<String> expectedColumns) throws Exception {
        List<String> actualColumns = new ArrayList<>();

        DataSource dataSource = lookup("java:jboss/datasources/ExampleDS", DataSource.class);
        Connection connection = dataSource.getConnection();

        try (PreparedStatement statement = connection.prepareStatement(QUERY_TABLE_COLUMNS)) {
            statement.setString(1, tableName.toUpperCase());

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                actualColumns.add(resultSet.getString("COLUMN_NAME").toLowerCase());
            }
        }

        Assert.assertEquals(expectedColumns, actualColumns);
    }

    protected void executeCliScript(File scriptFile) throws Exception {
        String jbossHome = System.getProperty("jboss.home.dir");
        String os = System.getProperty("os.name").toLowerCase();
        String jbossCli = "jboss-cli";

        if (os.contains("win")) {
            jbossCli += ".bat";
        } else {
            jbossCli += ".sh";
        }

        ProcessBuilder builder = new ProcessBuilder();
        builder.inheritIO();
        builder.command(jbossHome + "/bin/" + jbossCli, "--file=" + scriptFile.getAbsolutePath(), "-c");

        Process process = builder.start();
        process.waitFor();
    }
}
