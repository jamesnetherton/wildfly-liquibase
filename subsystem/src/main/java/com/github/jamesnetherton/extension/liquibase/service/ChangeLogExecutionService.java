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
import liquibase.resource.ResourceAccessor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.github.jamesnetherton.extension.liquibase.ChangeLogConfiguration;
import com.github.jamesnetherton.extension.liquibase.ChangeLogFormat;
import com.github.jamesnetherton.extension.liquibase.LiquibaseLogger;

import org.jboss.msc.service.AbstractService;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;

/**
 * Service which executes a Liquibase change log based on the provided {@link ChangeLogConfiguration}.
 */
public final class ChangeLogExecutionService extends AbstractService<ChangeLogExecutionService> {

    private static final String LIQUIBASE_ELEMENT_START = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\" \n" + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n"
            + "xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\" \n"
            + "xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.1.xsd\n"
            + "http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd\">\n";
    private static final String LIQUIBASE_ELEMENT_END = "</databaseChangeLog>";

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
            ResourceAccessor resourceAccessor = new WildFlyLiquibaseResourceAccessor(configuration);

            InitialContext initialContext = new InitialContext();
            DataSource datasource = (DataSource) initialContext.lookup(configuration.getDatasourceRef());
            connection = new JdbcConnection(datasource.getConnection());

            liquibase = new Liquibase(configuration.getFileName(), resourceAccessor, connection);

            String contextNames = configuration.getContextNames();
            if (contextNames != null) {
                liquibase.update(new Contexts(contextNames), new LabelExpression());
            } else {
                liquibase.update(new Contexts(), new LabelExpression());
            }
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

    private final class WildFlyLiquibaseResourceAccessor implements ResourceAccessor {

        private final ChangeLogConfiguration configuration;

        private WildFlyLiquibaseResourceAccessor(ChangeLogConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        public Set<InputStream> getResourcesAsStream(String path) throws IOException {
            InputStream resource = configuration.getClassLoader().getResourceAsStream(path);

            if (resource == null && !path.equals(configuration.getFileName())) {
                return null;
            }

            Set<InputStream> resources = new HashSet<>();

            if (resource != null) {
                resources.add(resource);
            } else {
                String definition = configuration.getDefinition();
                if (configuration.getFormat().equals(ChangeLogFormat.XML)) {
                    if (!definition.contains("http://www.liquibase.org/xml/ns/dbchangelog")) {
                        definition = LIQUIBASE_ELEMENT_START + definition;
                    }

                    if (!definition.contains(LIQUIBASE_ELEMENT_END)) {
                        definition += LIQUIBASE_ELEMENT_END;
                    }
                }
                resources.add(new ByteArrayInputStream(definition.getBytes(StandardCharsets.UTF_8)));
            }

            return resources;
        }

        @Override
        public Set<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
            HashSet<String> list = new HashSet<>();
            list.add(configuration.getFileName());
            return list;
        }

        @Override
        public ClassLoader toClassLoader() {
            return configuration.getClassLoader();
        }
    }
}
