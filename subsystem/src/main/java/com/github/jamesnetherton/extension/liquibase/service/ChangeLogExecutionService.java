/*-
 * #%L
 * wildfly-liquibase-subsystem
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
package com.github.jamesnetherton.extension.liquibase.service;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;

import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.github.jamesnetherton.extension.liquibase.ChangeLogConfiguration;
import com.github.jamesnetherton.extension.liquibase.LiquibaseLogger;
import com.github.jamesnetherton.extension.liquibase.resource.WildFlyLiquibaseResourceAccessor;

import org.jboss.msc.service.AbstractService;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;

/**
 * Service which executes a Liquibase change log based on the provided {@link ChangeLogConfiguration}.
 */
public final class ChangeLogExecutionService extends AbstractService<ChangeLogExecutionService> {

    private final ChangeLogConfiguration configuration;

    public ChangeLogExecutionService(ChangeLogConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void start(StartContext context) throws StartException {
        executeChangeLog(configuration);
    }

    @Override
    public ChangeLogExecutionService getValue() throws IllegalStateException {
        return this;
    }

    public void executeChangeLog(ChangeLogConfiguration configuration) {
        JdbcConnection connection = null;
        Liquibase liquibase = null;

        try {
            ResourceAccessor resourceAccessor = new CompositeResourceAccessor(new FileSystemResourceAccessor(), new WildFlyLiquibaseResourceAccessor(configuration));

            InitialContext initialContext = new InitialContext();
            DataSource datasource = (DataSource) initialContext.lookup(configuration.getDataSource());
            connection = new JdbcConnection(datasource.getConnection());

            Contexts contexts = new Contexts(configuration.getContextNames());
            LabelExpression labelExpression = new LabelExpression(configuration.getLabels());

            liquibase = new Liquibase(configuration.getFileName(), resourceAccessor, connection);
            liquibase.update(contexts, labelExpression);
        } catch (NamingException | LiquibaseException | SQLException e) {
            throw new IllegalStateException(e);
        } finally {
            if (liquibase != null && liquibase.getDatabase() != null) {
                try {
                    LiquibaseLogger.ROOT_LOGGER.info("Closing Liquibase database");
                    liquibase.getDatabase().close();
                } catch (DatabaseException e) {
                    LiquibaseLogger.ROOT_LOGGER.warn("Failed to close Liquibase database", e);
                }
            } else if (connection != null) {
                try {
                    LiquibaseLogger.ROOT_LOGGER.info("Closing database connection");
                    connection.close();
                } catch (DatabaseException e) {
                    LiquibaseLogger.ROOT_LOGGER.warn("Failed to close database connection", e);
                }
            }
        }
    }

    public static ServiceName createServiceName(String suffix) {
        return ServiceName.JBOSS.append("liquibase", "changelog", "execution", suffix);
    }
}
