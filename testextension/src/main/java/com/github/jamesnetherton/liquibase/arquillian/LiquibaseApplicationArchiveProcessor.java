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

import com.github.jamesnetherton.extension.liquibase.ChangeLogFormat;
import com.github.jamesnetherton.extension.liquibase.LiquibaseLogger;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

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
                    definition.dataSource().hashCode(),
                    definition.fileName().hashCode(),
                    definition.format().hashCode(),
                    definition.name().hashCode(),
                    clazz.getName().hashCode());

                ChangeLogFormat format = ChangeLogFormat.valueOf(definition.format().toUpperCase());

                InputStream inputStream = LiquibaseApplicationArchiveProcessor.class.getResourceAsStream("/changelogs/changelog" + format.getExtension());
                String changeLog = TestExtensionUtils.inputStreamToString(inputStream);

                changeLog = changeLog.replace("#TABLE_NAME#", tableName);
                changeLog = changeLog.replace("#DATASOURCE#", definition.dataSource());

                while (changeLog.contains("#ID#")) {
                    changeLog = changeLog.replaceFirst("#ID#", String.valueOf(CHANGESET_ID.incrementAndGet()));
                }

                String fileName = definition.fileName().isEmpty() ? String.format("%s-%s", tableName, format.getFileName()) : definition.fileName();
                StringAsset asset = new StringAsset(changeLog);
                if (applicationArchive instanceof JavaArchive) {
                    JavaArchive javaArchive = (JavaArchive) applicationArchive;
                    if (definition.resourceLocation().equals(ResourceLocation.CLASSPATH)) {
                        javaArchive.addAsResource(asset, fileName);
                    } else if (definition.resourceLocation().equals(ResourceLocation.META_INF)) {
                        javaArchive.addAsManifestResource(asset, fileName);
                    } else {
                        javaArchive.add(asset, fileName);
                    }
                } else if (applicationArchive instanceof WebArchive) {
                    WebArchive webArchive = (WebArchive) applicationArchive;
                    if (definition.resourceLocation().equals(ResourceLocation.CLASSPATH)) {
                        webArchive.addAsResource(asset, fileName);
                    } else if (definition.resourceLocation().equals(ResourceLocation.META_INF)) {
                        webArchive.addAsManifestResource(asset, fileName);
                    } else if (definition.resourceLocation().equals(ResourceLocation.WEB_INF)) {
                        webArchive.addAsWebInfResource(asset, fileName);
                    } else {
                        webArchive.add(asset, fileName);
                    }
                } else {
                    applicationArchive.add(asset, fileName);
                }

                if (definition.debug()) {
                    LiquibaseLogger.ROOT_LOGGER.info(applicationArchive.toString(true));
                    LiquibaseLogger.ROOT_LOGGER.info(changeLog);
                }
            }
        }
    }
}
