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
package com.github.jamesnetherton.extension.liquibase.deployment;

import java.util.List;

import com.github.jamesnetherton.extension.liquibase.ChangeLogConfiguration;
import com.github.jamesnetherton.extension.liquibase.LiquibaseConstants;
import com.github.jamesnetherton.extension.liquibase.service.ChangeLogExecutionService;
import com.github.jamesnetherton.extension.liquibase.service.ChangeLogConfigurationRegistryService;

import org.jboss.as.connector.subsystems.datasources.AbstractDataSourceService;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import static com.github.jamesnetherton.extension.liquibase.LiquibaseLogger.MESSAGE_DUPLICATE_DATASOURCE_REF;

/**
 * {@link DeploymentUnitProcessor} which adds a {@link ChangeLogExecutionService} service dependency for
 * the deployment unit.
 */
public class LiquibaseChangeLogExecutionProcessor implements DeploymentUnitProcessor{

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {

        DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();

        List<ChangeLogConfiguration> configurations = deploymentUnit.getAttachmentList(LiquibaseConstants.LIQUIBASE_CHANGELOGS);
        if (configurations.isEmpty()) {
            return;
        }

        ChangeLogConfigurationRegistryService registryService = getRegistryService(phaseContext.getServiceRegistry());

        for (ChangeLogConfiguration configuration : configurations) {
            String datasourceRef = configuration.getDatasourceRef();

            if (registryService.containsDatasourceRef(datasourceRef)) {
                throw new DeploymentUnitProcessingException(String.format(MESSAGE_DUPLICATE_DATASOURCE_REF, datasourceRef));
            }

            ServiceName serviceName = ChangeLogExecutionService.createServiceName(datasourceRef);
            ChangeLogExecutionService service = new ChangeLogExecutionService(configuration);
            ServiceBuilder<ChangeLogExecutionService> builder = phaseContext.getServiceTarget().addService(serviceName, service);

            ContextNames.BindInfo bindInfo = ContextNames.bindInfoFor(datasourceRef);
            ServiceName dataSourceServiceName = AbstractDataSourceService.getServiceName(bindInfo);
            builder.addDependency(dataSourceServiceName);

            builder.install();

            registryService.addConfiguration(getConfigurationKey(deploymentUnit, configuration), configuration);
        }
    }

    @Override
    public void undeploy(DeploymentUnit deploymentUnit) {
        List<ChangeLogConfiguration> configurations = deploymentUnit.getAttachmentList(LiquibaseConstants.LIQUIBASE_CHANGELOGS);
        if (!configurations.isEmpty()) {
            ChangeLogConfigurationRegistryService registryService = getRegistryService(deploymentUnit.getServiceRegistry());

            for (ChangeLogConfiguration configuration : configurations) {
                registryService.removeConfiguration(getConfigurationKey(deploymentUnit, configuration));
            }
        }
    }

    private String getConfigurationKey(DeploymentUnit deploymentUnit, ChangeLogConfiguration configuration) {
        return String.format("%s.%s", configuration.getName(), deploymentUnit.getName());
    }

    @SuppressWarnings("unchecked")
    private ChangeLogConfigurationRegistryService getRegistryService(ServiceRegistry serviceRegistry) {
        ServiceController<ChangeLogConfigurationRegistryService> controller = (ServiceController<ChangeLogConfigurationRegistryService>) serviceRegistry.getRequiredService(
                ChangeLogConfigurationRegistryService.getServiceName());
        return controller.getValue();
    }
}
