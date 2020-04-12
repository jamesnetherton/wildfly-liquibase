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
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import static com.github.jamesnetherton.extension.liquibase.LiquibaseLogger.MESSAGE_DUPLICATE_DATASOURCE_REF;

/**
 * Service which handles updates to the Liquibase subsystem DMR model.
 */
public class ChangeLogModelService extends AbstractService<ChangeLogModelService> {

    private final ChangeLogConfigurationRegistryService registryService;

    public ChangeLogModelService(ChangeLogConfigurationRegistryService registryService) {
        this.registryService = registryService;
    }

    public void createChangeLogModel(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        String changeLogName = operation.get(ModelDescriptionConstants.OP_ADDR).asObject().get(ModelConstants.DATABASE_CHANGELOG).asString();
        String changeLogDefinition = ChangeLogResource.VALUE.resolveModelAttribute(context, model).asString();
        String datasourceRef = ChangeLogResource.DATASOURCE_REF.resolveModelAttribute(context, model).asString();
        String contextNames = ChangeLogResource.CONTEXT_NAMES.resolveModelAttribute(context, model).asString();

        ChangeLogConfiguration configuration = ChangeLogConfiguration.builder()
            .name(changeLogName)
            .definition(changeLogDefinition)
            .datasourceRef(datasourceRef)
            .contextNames(contextNames)
            .classLoader(ChangeLogModelService.class.getClassLoader())
            .subsystemOrigin()
            .build();

        if (configuration.getFormat().equals(ChangeLogFormat.UNKNOWN)) {
            throw new OperationFailedException("Unable to determine change log format. Supported formats are JSON, SQL, YAML and XML");
        }

        ServiceTarget serviceTarget = context.getServiceTarget();
        ServiceName serviceName = ChangeLogExecutionService.createServiceName(datasourceRef);

        installChangeLogExecutionService(serviceTarget, serviceName, configuration);
    }

    public void updateChangeLogModel(OperationContext context, ModelNode operation) throws OperationFailedException {
        String changeLogName = operation.get(ModelDescriptionConstants.OP_ADDR).asObject().get(ModelConstants.DATABASE_CHANGELOG).asString();
        String value = operation.get(ModelDescriptionConstants.VALUE).asString();

        ChangeLogConfiguration configuration = registryService.removeConfiguration(changeLogName);
        if (configuration == null) {
            throw new OperationFailedException("Unable to update change log model. Existing configuration is null.");
        }

        String oldDatasourceRef = configuration.getDatasourceRef();

        switch (operation.get(ModelDescriptionConstants.NAME).asString()) {
            case ModelConstants.CONTEXT_NAMES:
                configuration.setContextNames(value);
                break;
            case ModelConstants.DATASOURCE_REF:
                configuration.setDatasourceRef(value);
                break;
            case ModelConstants.VALUE:
                configuration.setDefinition(value);
                break;
        }

        if (configuration.getFormat().equals(ChangeLogFormat.UNKNOWN)) {
            throw new OperationFailedException("Unable to determine change log format. Supported formats are JSON, SQL, YAML and XML");
        }

        String datasourceRef = configuration.getDatasourceRef();
        if (!oldDatasourceRef.equals(datasourceRef)) {
            throw new OperationFailedException("Modifying the change log datasource-ref property is not supported");
        }

        ServiceName serviceName = ChangeLogExecutionService.createServiceName(datasourceRef);
        ServiceTarget serviceTarget = context.getServiceTarget();

        context.removeService(serviceName);

        installChangeLogExecutionService(serviceTarget, serviceName, configuration);
    }

    public void removeChangeLogModel(OperationContext context, ModelNode model) throws OperationFailedException {
        String runtimeName = context.getCurrentAddressValue();
        String datasourceRef = ChangeLogResource.DATASOURCE_REF.resolveModelAttribute(context, model).asString();
        ServiceName serviceName = ChangeLogExecutionService.createServiceName(datasourceRef);
        context.removeService(serviceName);
        registryService.removeConfiguration(runtimeName);
    }

    public static ServiceName getServiceName() {
        return ServiceName.JBOSS.append("liquibase", "changelog", "model", "update");
    }

    @Override
    public ChangeLogModelService getValue() throws IllegalStateException {
        return this;
    }

    private void installChangeLogExecutionService(ServiceTarget serviceTarget, ServiceName serviceName, ChangeLogConfiguration configuration) throws OperationFailedException {
        if (registryService.containsDatasourceRef(configuration.getDatasourceRef())) {
            throw new OperationFailedException(String.format(MESSAGE_DUPLICATE_DATASOURCE_REF, configuration.getDatasourceRef()));
        }

        ChangeLogExecutionService service = new ChangeLogExecutionService(configuration);
        ServiceBuilder<ChangeLogExecutionService> builder = serviceTarget.addService(serviceName, service);

        ContextNames.BindInfo bindInfo = ContextNames.bindInfoFor(configuration.getDatasourceRef());
        ServiceName dataSourceServiceName = AbstractDataSourceService.getServiceName(bindInfo);

        builder.addDependency(dataSourceServiceName);
        builder.install();

        registryService.addConfiguration(configuration.getName(), configuration);
    }
}
