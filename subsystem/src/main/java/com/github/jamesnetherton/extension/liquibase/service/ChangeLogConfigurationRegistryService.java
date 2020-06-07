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

import java.util.HashMap;
import java.util.Map;

import com.github.jamesnetherton.extension.liquibase.ChangeLogConfiguration;

public class ChangeLogConfigurationRegistryService {

    private final Map<String, ChangeLogConfiguration> configurationMap = new HashMap<>();

    public void addConfiguration(String runtimeName, ChangeLogConfiguration configuration) {
        synchronized (configurationMap) {
            configurationMap.put(runtimeName, configuration);
        }
    }

    public ChangeLogConfiguration removeConfiguration(String runtimeName) {
        synchronized (configurationMap) {
            return configurationMap.remove(runtimeName);
        }
    }

    public boolean containsDatasource(String dataSource) {
        synchronized (configurationMap) {
            return configurationMap.values()
                .stream()
                .anyMatch((configuration -> configuration.getDataSource().equals(dataSource)));
        }
    }
}
