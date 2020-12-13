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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.jboss.arquillian.test.spi.TestEnricher;

/**
 * {@link TestEnricher} to inject the database table name used by the given test class for the associated change log
 */
public final class ChangeLogDefinitionEnricher implements TestEnricher {

    @Override
    public void enrich(Object testClass) {
        try {
            Class<?> clazz = testClass.getClass();
            for (Field field : clazz.getDeclaredFields()) {
                ChangeLogDefinition definition = field.getAnnotation(ChangeLogDefinition.class);
                if (definition != null && field.getType() == String.class) {
                    String tableName = LiquibaseExtensionUtils.generateTableName(definition.name(),
                        definition.dataSource().hashCode(),
                        definition.fileName().hashCode(),
                        definition.format().hashCode(),
                        definition.name().hashCode(),
                        clazz.getName().hashCode());
                    field.setAccessible(true);
                    field.set(testClass, tableName);
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Object[] resolve(Method method) {
        return null;
    }
}
