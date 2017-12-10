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

import java.util.ArrayList;
import java.util.List;

import com.github.jamesnetherton.extension.liquibase.ChangeLogConfiguration;
import com.github.jamesnetherton.extension.liquibase.ChangeLogFormat;
import com.github.jamesnetherton.extension.liquibase.ChangeLogResource;
import com.github.jamesnetherton.extension.liquibase.ModelConstants;

import org.jboss.as.connector.subsystems.datasources.AbstractDataSourceService;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.AbstractService;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;

/**
 * Service which handles updates to the Liquibase subsystem DMR model.
 */
public class ChangeLogModelUpdateService extends AbstractService<ChangeLogModelUpdateService> {

    public void updateChangeLogModel(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        updateChangeLogModel(context, operation, model, null);
    }

    public void updateChangeLogModel(OperationContext context, ModelNode operation, ModelNode futureState, ModelNode currentState) throws OperationFailedException {
        String changeLogName = operation.get(ModelDescriptionConstants.OP_ADDR).asObject().get(ModelConstants.DATABASE_CHANGELOG).asString();
        String changeLogDefinition = ChangeLogResource.VALUE.resolveModelAttribute(context, futureState).asString();
        String datasourceRef = ChangeLogResource.DATASOURCE_REF.resolveModelAttribute(context, futureState).asString();
        String contextNames = ChangeLogResource.CONTEXT_NAMES.resolveModelAttribute(context, futureState).asString();

        ChangeLogConfiguration configuration = ChangeLogConfiguration.builder()
            .name(changeLogName)
            .definition(changeLogDefinition)
            .datasourceRef(datasourceRef)
            .contextNames(contextNames)
            .classLoader(ChangeLogModelUpdateService.class.getClassLoader())
            .build();

        if (configuration.getFormat().equals(ChangeLogFormat.UNKNOWN)) {
            throw new OperationFailedException("Unable to determine change log format. Supported formats are JSON, YAML and XML");
        }

        List<ChangeLogConfiguration> configurations = new ArrayList<>();
        configurations.add(configuration);

        ServiceName serviceName = ChangeLogExecutionService.createServiceName(context.getCurrentAddressValue());
        ServiceTarget serviceTarget = context.getServiceTarget();

        if (currentState != null) {
            String oldDatasourceRef = ChangeLogResource.DATASOURCE_REF.resolveModelAttribute(context, currentState).asString();

            if (!oldDatasourceRef.equals(datasourceRef)) {
                // Datasource JNDI reference has changed. Remove service and create a new one
                context.removeService(serviceName);

                ChangeLogExecutionService service = new ChangeLogExecutionService(configurations);
                ServiceBuilder<ChangeLogExecutionService> builder = serviceTarget.addService(serviceName, service);

                ContextNames.BindInfo bindInfo = ContextNames.bindInfoFor(configuration.getDatasourceRef());
                ServiceName dataSourceServiceName = AbstractDataSourceService.getServiceName(bindInfo);

                builder.addDependency(dataSourceServiceName);
                builder.install();
            } else {
                // Update configuration and execute change log
                ServiceController<ChangeLogExecutionService> controller = (ServiceController<ChangeLogExecutionService>) context.getServiceRegistry(false).getService(serviceName);
                ChangeLogExecutionService service = controller.getValue();

                // TODO: Clean up this hack
                service.setConfigurations(configurations);
                configurations.forEach(c -> service.executeChangeLog(c));
            }
        } else {
            // TODO: Maybe this should be a static method call?

            // No current state means we're adding change log configuration for the first time
            ChangeLogExecutionService service = new ChangeLogExecutionService(configurations);
            ServiceBuilder<ChangeLogExecutionService> builder = serviceTarget.addService(serviceName, service);

            ContextNames.BindInfo bindInfo = ContextNames.bindInfoFor(configuration.getDatasourceRef());
            ServiceName dataSourceServiceName = AbstractDataSourceService.getServiceName(bindInfo);

            builder.addDependency(dataSourceServiceName);
            builder.install();
        }
    }

    public static ServiceName createServiceName() {
        return ServiceName.JBOSS.append("liquibase", "changelog", "model", "update");
    }

    @Override
    public ChangeLogModelUpdateService getValue() throws IllegalStateException {
        return this;
    }
}
