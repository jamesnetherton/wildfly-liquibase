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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ChangeLogConfiguration {

    private String contexts;
    private String dataSource;
    private String definition;
    private String deployment;
    private boolean failOnError = true;
    private String hostExcludes;
    private String hostIncludes;
    private String labels;
    private String name;
    private String path;
    private ClassLoader classLoader;
    private ConfigurationOrigin origin;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDeployment() {
        return deployment;
    }

    public void setDeployment(String deployment) {
        this.deployment = deployment;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public void setContexts(String contexts) {
        this.contexts = contexts;
    }

    public String getContexts() {
        return contexts;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public String getHostExcludes() {
        return hostExcludes;
    }

    public void setHostExcludes(String hostExcludes) {
        this.hostExcludes = hostExcludes;
    }

    public String getHostIncludes() {
        return hostIncludes;
    }

    public void setHostIncludes(String hostIncludes) {
        this.hostIncludes = hostIncludes;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getLabels() {
        return labels;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setOrigin(ConfigurationOrigin origin) {
        this.origin = origin;
    }

    public ConfigurationOrigin getOrigin() {
        return origin;
    }

    public boolean isSubsystemOrigin() {
        return getOrigin().equals(ConfigurationOrigin.SUBSYSTEM);
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

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ChangeLogConfiguration that = (ChangeLogConfiguration) o;
        return failOnError == that.failOnError && Objects.equals(name, that.name) && Objects.equals(path, that.path) && Objects.equals(contexts, that.contexts)
                && Objects.equals(dataSource, that.dataSource) && Objects.equals(definition, that.definition) && Objects.equals(deployment, that.deployment) && Objects
                .equals(hostExcludes, that.hostExcludes) && Objects.equals(hostIncludes, that.hostIncludes) && Objects.equals(labels, that.labels) && Objects
                .equals(classLoader, that.classLoader) && origin == that.origin;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path, contexts, dataSource, definition, deployment, failOnError, hostExcludes, hostIncludes, labels, classLoader, origin);
    }

    public static class Builder {
        private String contexts;
        private String dataSource;
        private String definition;
        private String deployment;
        private boolean failOnError = true;
        private String hostExcludes;
        private String hostIncludes;
        private String labels;
        private String name;
        private String path;
        private ClassLoader classLoader;
        private ConfigurationOrigin origin;

        public Builder contexts(String contexts) {
            this.contexts = contexts;
            return this;
        }

        public Builder dataSource(String dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        public Builder definition(String definition) {
            this.definition = definition;
            return this;
        }

        public Builder deployment(String deployment) {
            this.deployment = deployment;
            return this;
        }

        public Builder hostExcludes(String hostExcludes) {
            this.hostExcludes = hostExcludes;
            return this;
        }

        public Builder hostIncludes(String hostIncludes) {
            this.hostIncludes = hostIncludes;
            return this;
        }

        public Builder failOnError(boolean failOnError) {
            this.failOnError = failOnError;
            return this;
        }

        public Builder labels(String labels) {
            this.labels = labels;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder classLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        public Builder deploymentOrigin() {
            this.origin = ConfigurationOrigin.DEPLOYMENT;
            return this;
        }

        public Builder subsystemOrigin() {
            this.origin = ConfigurationOrigin.SUBSYSTEM;
            return this;
        }

        public ChangeLogConfiguration build() {
            if (this.name == null) {
                throw new IllegalStateException("ChangeLogConfiguration name must be specified");
            }

            if (this.definition == null) {
                throw new IllegalStateException("ChangeLogConfiguration definition must be specified");
            }

            if (this.dataSource == null) {
                throw new IllegalStateException("ChangeLogConfiguration dataSource must be specified");
            }

            if (this.classLoader == null) {
                throw new IllegalStateException("ChangeLogConfiguration classLoader must be specified");
            }

            if (this.origin == null) {
                throw new IllegalStateException("ChangeLogConfiguration origin must be specified");
            }

            ChangeLogConfiguration configuration = new ChangeLogConfiguration();
            configuration.setContexts(this.contexts);
            configuration.setClassLoader(this.classLoader);
            configuration.setDataSource(this.dataSource);
            configuration.setDefinition(this.definition);
            configuration.setDeployment(this.deployment);
            configuration.setFailOnError(this.failOnError);
            configuration.setHostExcludes(this.hostExcludes);
            configuration.setHostIncludes(this.hostIncludes);
            configuration.setLabels(this.labels);
            configuration.setName(this.name);
            configuration.setOrigin(this.origin);
            configuration.setPath(this.path);
            return configuration;
        }

        private String getName() {
            return this.name;
        }
    }

    public static class BuilderCollection {
        private final List<Builder> builders = new ArrayList<>();

        public List<Builder> getBuilders() {
            return Collections.unmodifiableList(builders);
        }

        public void addBuilder(Builder builder) {
            builders.add(builder);
        }

        public Builder getOrCreateBuilder(String name) {
            for (Builder builder : builders) {
                if (builder.getName().equals(name)) {
                    return builder;
                }
            }
            return builder();
        }
    }

    enum ConfigurationOrigin {
        DEPLOYMENT,
        SUBSYSTEM;

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }
}
