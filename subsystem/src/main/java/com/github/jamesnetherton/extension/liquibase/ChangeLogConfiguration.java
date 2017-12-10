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
package com.github.jamesnetherton.extension.liquibase;

public final class ChangeLogConfiguration {

    private String name;
    private String definition;
    private String datasourceRef;
    private String contextNames;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getDatasourceRef() {
        return datasourceRef;
    }

    public void setDatasourceRef(String datasourceRef) {
        this.datasourceRef = datasourceRef;
    }

    public void setContextNames(String contextNames) {
        this.contextNames = contextNames;
    }

    public String getContextNames() {
        return contextNames;
    }

    public String getFileName() {
        if (this.name == null) {
            return null;
        }

        if (this.name.toLowerCase().matches(".*\\.(json|sql|xml|yaml|yml)")) {
            return this.name;
        }

        return this.name + getFormat().getExtension();
    }

    public ChangeLogFormat getFormat() {
        // Try to work out change log format from the name attribute
        ChangeLogFormat format = ChangeLogFormat.fromFileName(this.name);
        if (!format.equals(ChangeLogFormat.UNKNOWN)) {
            return format;
        }

        // Else try to make some assumptions based on the change log content
        if (this.definition.contains("<databaseChangeLog") || this.definition.contains("<changeSet")) {
            return ChangeLogFormat.XML;
        } else if (this.definition.contains("databaseChangeLog:")) {
            return ChangeLogFormat.YAML;
        } else if (this.definition.contains("\"databaseChangeLog\":")) {
            return ChangeLogFormat.JSON;
        } else if (this.definition.contains("--changeset")) {
            return ChangeLogFormat.SQL;
        } else {
            return ChangeLogFormat.UNKNOWN;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ChangeLogConfiguration that = (ChangeLogConfiguration) o;

        if (!name.equals(that.name))
            return false;
        if (!definition.equals(that.definition))
            return false;
        if (!datasourceRef.equals(that.datasourceRef))
            return false;
        return contextNames != null ? contextNames.equals(that.contextNames) : that.contextNames == null;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + definition.hashCode();
        result = 31 * result + datasourceRef.hashCode();
        result = 31 * result + (contextNames != null ? contextNames.hashCode() : 0);
        return result;
    }
}
