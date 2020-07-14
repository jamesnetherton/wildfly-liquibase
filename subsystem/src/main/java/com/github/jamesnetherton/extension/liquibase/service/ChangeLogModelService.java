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

import liquibase.Liquibase;

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
import static com.github.jamesnetherton.extension.liquibase.LiquibaseLogger.MESSAGE_DUPLICATE_DATASOURCE;

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
        String contexts = ChangeLogResource.CONTEXTS.resolveModelAttribute(context, model).asString("");
        String dataSource = ChangeLogResource.DATASOURCE.resolveModelAttribute(context, model).asString();
        Boolean failOnError = ChangeLogResource.FAIL_ON_ERROR.resolveModelAttribute(context, model).asBoolean(true);
        String hostExcludes = ChangeLogResource.HOST_EXCLUDES.resolveModelAttribute(context, model).asString("");
        String hostIncludes = ChangeLogResource.HOST_INCLUDES.resolveModelAttribute(context, model).asString("");
        String labels = ChangeLogResource.LABELS.resolveModelAttribute(context, model).asString("");

        ChangeLogConfiguration configuration = ChangeLogConfiguration.builder()
            .contexts(contexts)
            .classLoader(Liquibase.class.getClassLoader())
            .dataSource(dataSource)
            .definition(changeLogDefinition)
            .failOnError(failOnError)
            .hostExcludes(hostExcludes)
            .hostIncludes(hostIncludes)
            .labels(labels)
            .name(changeLogName)
            .subsystemOrigin()
            .build();

        if (configuration.getFormat().equals(ChangeLogFormat.UNKNOWN)) {
            throw new OperationFailedException("Unable to determine change log format. Supported formats are JSON, SQL, YAML and XML");
        }

        ServiceTarget serviceTarget = context.getServiceTarget();
        ServiceName serviceName = ChangeLogExecutionService.createServiceName(configuration.getName());

        installChangeLogExecutionService(serviceTarget, serviceName, configuration);
    }

    public void updateChangeLogModel(OperationContext context, ModelNode operation) throws OperationFailedException {
        String changeLogName = operation.get(ModelDescriptionConstants.OP_ADDR).asObject().get(ModelConstants.DATABASE_CHANGELOG).asString();
        String value = operation.get(ModelDescriptionConstants.VALUE).asString();

        ChangeLogConfiguration configuration = registryService.removeConfiguration(changeLogName);
        if (configuration == null) {
            throw new OperationFailedException("Unable to update change log model. Existing configuration is null.");
        }

        String oldDataSource = configuration.getDataSource();

        switch (operation.get(ModelDescriptionConstants.NAME).asString()) {
            case ModelConstants.CONTEXTS:
                configuration.setContexts(value);
                break;
            case ModelConstants.DATASOURCE:
                configuration.setDataSource(value);
                break;
            case ModelConstants.FAIL_ON_ERROR:
                configuration.setFailOnError(Boolean.valueOf(value));
                break;
            case ModelConstants.HOST_EXCLUDES:
                configuration.setHostExcludes(value);
                break;
            case ModelConstants.HOST_INCLUDES:
                configuration.setHostIncludes(value);
                break;
            case ModelConstants.LABELS:
                configuration.setLabels(value);
                break;
            case ModelConstants.VALUE:
                configuration.setDefinition(value);
                break;
        }

        if (configuration.getFormat().equals(ChangeLogFormat.UNKNOWN)) {
            throw new OperationFailedException("Unable to determine change log format. Supported formats are JSON, SQL, YAML and XML");
        }

        String dataSource = configuration.getDataSource();
        if (!oldDataSource.equals(dataSource)) {
            throw new OperationFailedException("Modifying the change log datasource property is not supported");
        }

        ServiceName serviceName = ChangeLogExecutionService.createServiceName(changeLogName);
        ServiceTarget serviceTarget = context.getServiceTarget();

        context.removeService(serviceName);

        installChangeLogExecutionService(serviceTarget, serviceName, configuration);
    }

    public void removeChangeLogModel(OperationContext context, ModelNode model) throws OperationFailedException {
        String runtimeName = context.getCurrentAddressValue();
        ServiceName serviceName = ChangeLogExecutionService.createServiceName(runtimeName);
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
        if (registryService.containsDatasource(configuration.getDataSource())) {
            throw new OperationFailedException(String.format(MESSAGE_DUPLICATE_DATASOURCE, configuration.getDataSource()));
        }

        ChangeLogExecutionService service = new ChangeLogExecutionService(configuration);
        ServiceBuilder<ChangeLogExecutionService> builder = serviceTarget.addService(serviceName, service);

        ContextNames.BindInfo bindInfo = ContextNames.bindInfoFor(configuration.getDataSource());
        ServiceName dataSourceServiceName = AbstractDataSourceService.getServiceName(bindInfo);

        builder.addDependency(dataSourceServiceName);
        builder.install();

        registryService.addConfiguration(configuration.getName(), configuration);
    }
}
