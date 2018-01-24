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

import com.github.jamesnetherton.extension.liquibase.deployment.LiquibaseChangeLogExecutionProcessor;
import com.github.jamesnetherton.extension.liquibase.deployment.LiquibaseChangeLogParseProcessor;
import com.github.jamesnetherton.extension.liquibase.deployment.LiquibaseDependenciesProcessor;
import com.github.jamesnetherton.extension.liquibase.service.ChangeLogModelService;
import com.github.jamesnetherton.extension.liquibase.service.ChangeLogConfigurationRegistryService;
import com.github.jamesnetherton.extension.liquibase.service.ServiceHelper;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.deployment.Phase;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;

class LiquibaseSubsystemAdd extends AbstractBoottimeAddStepHandler {

    private static final int DEPENDENCIES_LIQUIBASE = Phase.DEPENDENCIES_SINGLETON_DEPLOYMENT + 0x01;
    private static final int INSTALL_LIQUIBASE_CHANGE_LOG = Phase.INSTALL_MDB_DELIVERY_DEPENDENCIES + 0x01;
    private static final int INSTALL_LIQUIBASE_MIGRATION_EXECUTION = INSTALL_LIQUIBASE_CHANGE_LOG + 0x01;

    @Override
    protected void performBoottime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        LiquibaseLogger.ROOT_LOGGER.info("Activating Liquibase Subsystem");

        ServiceTarget serviceTarget = context.getServiceTarget();

        ServiceName registryServiceName = ChangeLogConfigurationRegistryService.getServiceName();
        ChangeLogConfigurationRegistryService registryService = new ChangeLogConfigurationRegistryService();
        ServiceHelper.installService(registryServiceName, serviceTarget, registryService);

        ServiceName modelUpdateServiceName = ChangeLogModelService.getServiceName();
        ChangeLogModelService modelUpdateService = new ChangeLogModelService(registryService);
        ServiceHelper.installService(modelUpdateServiceName, serviceTarget, modelUpdateService);

        context.addStep(new AbstractDeploymentChainStep() {
            public void execute(DeploymentProcessorTarget processorTarget) {
                processorTarget.addDeploymentProcessor(LiquibaseExtension.SUBSYSTEM_NAME, Phase.DEPENDENCIES, DEPENDENCIES_LIQUIBASE, new LiquibaseDependenciesProcessor());
                processorTarget.addDeploymentProcessor(LiquibaseExtension.SUBSYSTEM_NAME, Phase.INSTALL, INSTALL_LIQUIBASE_CHANGE_LOG, new LiquibaseChangeLogParseProcessor());
                processorTarget.addDeploymentProcessor(LiquibaseExtension.SUBSYSTEM_NAME, Phase.INSTALL, INSTALL_LIQUIBASE_MIGRATION_EXECUTION, new LiquibaseChangeLogExecutionProcessor());
            }
        }, OperationContext.Stage.RUNTIME);
    }
}
