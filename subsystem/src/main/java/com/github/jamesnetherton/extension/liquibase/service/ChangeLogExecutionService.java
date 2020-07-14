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
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.github.jamesnetherton.extension.liquibase.ChangeLogConfiguration;
import com.github.jamesnetherton.extension.liquibase.LiquibaseLogger;
import com.github.jamesnetherton.extension.liquibase.resource.WildFlyResourceAccessor;

import org.jboss.msc.service.AbstractService;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;

/**
 * Service which executes a Liquibase change log based on the provided {@link ChangeLogConfiguration}.
 */
public final class ChangeLogExecutionService extends AbstractService<ChangeLogExecutionService> {

    private static final AtomicInteger COUNTER = new AtomicInteger();
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
        if (!ServiceHelper.isChangeLogExecutable(configuration)) {
            LiquibaseLogger.ROOT_LOGGER.info("Not executing changelog {} as host-excludes or host-includes rules did not apply to this server host", configuration.getFileName());
            return;
        }

        JdbcConnection connection = null;
        Liquibase liquibase = null;

        final ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        try {
            ResourceAccessor resourceAccessor = new CompositeResourceAccessor(new FileSystemResourceAccessor(), new WildFlyResourceAccessor(configuration));

            InitialContext initialContext = new InitialContext();
            DataSource datasource = (DataSource) initialContext.lookup(configuration.getDataSource());
            connection = new JdbcConnection(datasource.getConnection());

            Contexts contexts = new Contexts(configuration.getContexts());
            LabelExpression labelExpression = new LabelExpression(configuration.getLabels());

            LiquibaseLogger.ROOT_LOGGER.info(String.format("Starting execution of %s changelog %s", configuration.getOrigin(), configuration.getFileName()));
            Thread.currentThread().setContextClassLoader(configuration.getClassLoader());
            liquibase = new Liquibase(configuration.getFileName(), resourceAccessor, connection);
            liquibase.update(contexts, labelExpression);
        } catch (NamingException | LiquibaseException | SQLException e) {
            if (configuration.isFailOnError()) {
                throw new IllegalStateException(e);
            } else {
                LiquibaseLogger.ROOT_LOGGER.warn("Liquibase changelog execution failed:", e);
                LiquibaseLogger.ROOT_LOGGER.warn("Continuing deployment after changelog execution failure of {} as fail-on-error is false", configuration.getDeployment());
            }
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
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
    }

    public static ServiceName createServiceName(String changeLogName) {
        String suffix = String.format("%s.%d", changeLogName, COUNTER.incrementAndGet());
        return ServiceName.JBOSS.append("liquibase", "changelog", "execution", suffix);
    }
}
