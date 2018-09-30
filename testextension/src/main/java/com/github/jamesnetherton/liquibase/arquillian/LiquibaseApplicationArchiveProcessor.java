/*-
 * #%L
 * wildfly-liquibase-testextension
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.jamesnetherton.extension.liquibase.ChangeLogFormat;
import com.github.jamesnetherton.extension.liquibase.LiquibaseLogger;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;

/**
 * {@link ApplicationArchiveProcessor} for automatically adding Liquibase change log file(s) to a test deployment.
 *
 * The format of the change log is determined by the {@link ChangeLogDefinition} annotation.
 *
 * Template placeholder values are replaced to ensure that database table names and change set ids are unique for each test class.
 */
public final class LiquibaseApplicationArchiveProcessor implements ApplicationArchiveProcessor {

    private static final AtomicInteger CHANGESET_ID = new AtomicInteger();

    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {
        if (applicationArchive instanceof EnterpriseArchive) {
            return;
        }

        Class<?> clazz = testClass.getJavaClass();

        for (Field field : clazz.getDeclaredFields()) {
            ChangeLogDefinition definition = field.getAnnotation(ChangeLogDefinition.class);
            if (definition != null) {
                String tableName = LiquibaseExtensionUtils.generateTableName(definition.name(),
                    definition.datasourceRef().hashCode(),
                    definition.fileName().hashCode(),
                    definition.format().hashCode(),
                    definition.name().hashCode(),
                    clazz.getName().hashCode());

                ChangeLogFormat format = ChangeLogFormat.valueOf(definition.format().toUpperCase());

                InputStream inputStream = LiquibaseApplicationArchiveProcessor.class.getResourceAsStream("/changelogs/changelog" + format.getExtension());
                String changeLog = inputStreamToString(inputStream);

                changeLog = changeLog.replace("#TABLE_NAME#", tableName);
                changeLog = changeLog.replace("#DATASOURCE_REF#", definition.datasourceRef());

                while (changeLog.contains("#ID#")) {
                    changeLog = changeLog.replaceFirst("#ID#", String.valueOf(CHANGESET_ID.incrementAndGet()));
                }

                if (definition.debug()) {
                    LiquibaseLogger.ROOT_LOGGER.info(changeLog);
                }

                String fileName = definition.fileName().isEmpty() ? String.format("%s-%s", tableName, format.getFileName()) : definition.fileName();
                applicationArchive.add(new StringAsset(changeLog), fileName);
            }
        }
    }

    private String inputStreamToString(InputStream in) {
        if (in == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }

        StringBuilder builder = new StringBuilder();
        String line;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))){
            while ((line = reader.readLine()) != null) {
                builder.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading InputStream " + in);
        }
        return builder.toString();
    }
}
