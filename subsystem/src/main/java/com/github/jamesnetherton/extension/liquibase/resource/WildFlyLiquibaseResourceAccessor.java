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
package com.github.jamesnetherton.extension.liquibase.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import com.github.jamesnetherton.extension.liquibase.ChangeLogConfiguration;
import com.github.jamesnetherton.extension.liquibase.ChangeLogFormat;

public final class WildFlyLiquibaseResourceAccessor extends VFSResourceAccessor {

    private static final String LIQUIBASE_ELEMENT_START = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\" \n" + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n"
        + "xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\" \n"
        + "xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd\n"
        + "http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd\">\n";
    private static final String LIQUIBASE_ELEMENT_END = "</databaseChangeLog>";

    public WildFlyLiquibaseResourceAccessor(ChangeLogConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Set<InputStream> getResourcesAsStream(String path) throws IOException {
        Set<InputStream> resources = new HashSet<>();
        InputStream resource = configuration.getClassLoader().getResourceAsStream(path);

        if (resource == null) {
            String definition = configuration.getDefinition();
            if (configuration.isSubsystemOrigin() && configuration.getFormat().equals(ChangeLogFormat.XML)) {
                if (!definition.contains("http://www.liquibase.org/xml/ns/dbchangelog")) {
                    definition = LIQUIBASE_ELEMENT_START + definition;
                }

                if (!definition.contains(LIQUIBASE_ELEMENT_END)) {
                    definition += LIQUIBASE_ELEMENT_END;
                }
            }

            if (path.equals(configuration.getFileName())) {
                resources.add(new ByteArrayInputStream(definition.getBytes(StandardCharsets.UTF_8)));
            } else {
                resources = super.getResourcesAsStream(path);
                if (resources == null || resources.isEmpty()) {
                    // Attempt to work out the 'relative to' change log path
                    String parentPath =  configuration.getPath().replace("/content/" + configuration.getDeployment(), "");
                    parentPath = parentPath.replace(configuration.getFileName(), "");
                    resource = configuration.getClassLoader().getResourceAsStream(parentPath + path);
                    if (resource != null) {
                        resources = new HashSet<>();
                        resources.add(resource);
                    }
                }
            }
        } else {
            resources.add(resource);
        }

        return resources;
    }
}
