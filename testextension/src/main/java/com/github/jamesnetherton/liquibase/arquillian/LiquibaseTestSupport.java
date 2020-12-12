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
package com.github.jamesnetherton.liquibase.arquillian;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentHelper;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiquibaseTestSupport {

    protected static final Logger LOG = LoggerFactory.getLogger(LiquibaseTestSupport.class);
    protected static final List<String> DEFAULT_COLUMNS = Arrays.asList("firstname", "id", "lastname", "state", "username");
    private static final String QUERY_TABLES = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES";
    private static final String QUERY_TABLE_COLUMNS = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? ORDER BY COLUMN_NAME ASC";
    private static final String DEFAULT_CLI_SCRIPT_TIMEOUT = "60000";
    private static final String EXAMPLE_DS = "java:jboss/datasources/ExampleDS";

    @ArquillianResource
    private InitialContext context;

    @ArquillianResource
    private ManagementClient managementClient;

    @SuppressWarnings("unchecked")
    protected <T> T lookup(String name, Class<?> T) throws Exception {
        return (T) context.lookup(name);
    }

    protected void debugDatabase() throws Exception {
        DataSource dataSource = lookup(EXAMPLE_DS, DataSource.class);
        try(Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(QUERY_TABLES)) {
                ResultSet resultSet = statement.executeQuery();
                LOG.info("=========> Tables <=========");
                while (resultSet.next()) {
                    LOG.info(resultSet.getString("TABLE_NAME"));
                }
            }
        }
    }

    protected void assertTableModified(String tableName) throws Exception {
        assertTableModified(tableName, DEFAULT_COLUMNS);
    }

    protected void assertTableModified(String tableName, List<String> expectedColumns) throws Exception {
        assertTableModified(tableName, expectedColumns, EXAMPLE_DS);
    }

    protected void assertTableModified(String tableName, List<String> expectedColumns, String dsJndiName) throws Exception {
        List<String> actualColumns = new ArrayList<>();

        DataSource dataSource = lookup(dsJndiName, DataSource.class);

        try(Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(QUERY_TABLE_COLUMNS)) {
                statement.setString(1, tableName.toUpperCase());

                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    actualColumns.add(resultSet.getString("COLUMN_NAME").toLowerCase());
                }
            }
        }

        Assert.assertEquals(expectedColumns, actualColumns);
    }

    protected boolean removeLiquibaseDmrModel(String name) throws Exception {
        return executeCliCommand("/subsystem=liquibase/databaseChangeLog=" + name + "/:remove");
    }

    protected boolean addDataSource(String dataSourceName, String databaseName) throws Exception {
        String command = "data-source add --name=" + dataSourceName + " --jndi-name=java:jboss/datasources/" + dataSourceName + " --driver-name=h2 --connection-url=jdbc:h2:mem:" + databaseName + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE --user-name=sa --password=sa";
        return executeCliCommand(command);
    }

    protected boolean removeDataSource(String dataSourceName) throws Exception {
        return executeCliCommand("data-source remove --name=" + dataSourceName);
    }

    protected boolean deploy(String deployment) throws Exception {
        return executeCliCommand("deploy " + deployment);
    }

    protected boolean undeploy(String deployment) throws Exception {
        return executeCliCommand("undeploy " + deployment);
    }

    protected boolean executeCliScript(File scriptFile) throws Exception {
        return jbossCli("--file=" + scriptFile.getAbsolutePath());
    }

    protected boolean executeCliCommand(String command) throws Exception {
        return jbossCli("--command=" + command);
    }

    protected void executeSqlScript(InputStream script, String datasourceBinding) throws Exception {
        if (script == null) {
            throw new IllegalArgumentException("Script InputStream cannot be null");
        }

        try {
            InitialContext initialContext = new InitialContext();
            DataSource dataSource = (DataSource) initialContext.lookup(datasourceBinding);

            try (Connection connection = dataSource.getConnection()) {
                try (Statement statement = connection.createStatement()) {
                    String sql = TestExtensionUtils.inputStreamToString(script);
                    statement.execute(sql);
                    LOG.info("\nExecuted database initialization script\n{}", sql);
                }
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        } catch (NamingException e) {
            throw new IllegalStateException(e);
        }
    }

    protected String deployChangeLog(String originalFileName, String runtimeName) throws Exception {
        URL url = getClass().getResource("/" + originalFileName);
        ServerDeploymentHelper server = new ServerDeploymentHelper(managementClient.getControllerClient());
        return server.deploy(runtimeName, url.openStream());
    }

    protected void undeployChangeLog(String runtimeName) throws Exception {
        ServerDeploymentHelper server = new ServerDeploymentHelper(managementClient.getControllerClient());
        server.undeploy(runtimeName);
    }

    private boolean jbossCli(String command) throws Exception {
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
        builder.environment().put("NOPAUSE", "Y");
        builder.command(jbossHome + "/bin/" + jbossCli, "-c", command, "--timeout=" + DEFAULT_CLI_SCRIPT_TIMEOUT);

        Process process = builder.start();
        process.waitFor();

        return process.exitValue() == 0;
    }
}
