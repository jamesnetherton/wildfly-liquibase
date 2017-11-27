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
import com.github.jamesnetherton.extension.liquibase.service.ChangeLogModelUpdateService;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.deployment.Phase;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;

class LiquibaseSubsystemAdd extends AbstractBoottimeAddStepHandler {

    private static final int POST_MODULE_LIQUIBASE_CHANGE_LOG = Phase.POST_MODULE_PERMISSIONS_VALIDATION + 0x01;
    private static final int INSTALL_LIQUIBASE_MIGRATION_EXECUTION = Phase.INSTALL_BUNDLE_ACTIVATE + 0x01;

    @Override
    protected void performBoottime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        LiquibaseLogger.ROOT_LOGGER.info("Activating Liquibase Subsystem");

        ServiceName serviceName = ChangeLogModelUpdateService.createServiceName();
        ChangeLogModelUpdateService service = new ChangeLogModelUpdateService();

        ServiceBuilder<ChangeLogModelUpdateService> builder = context.getServiceTarget().addService(serviceName, service);
        builder.install();

        context.addStep(new AbstractDeploymentChainStep() {
            public void execute(DeploymentProcessorTarget processorTarget) {
                processorTarget.addDeploymentProcessor(LiquibaseExtension.SUBSYSTEM_NAME, Phase.POST_MODULE, POST_MODULE_LIQUIBASE_CHANGE_LOG, new LiquibaseChangeLogParseProcessor());
                processorTarget.addDeploymentProcessor(LiquibaseExtension.SUBSYSTEM_NAME, Phase.INSTALL, INSTALL_LIQUIBASE_MIGRATION_EXECUTION, new LiquibaseChangeLogExecutionProcessor());
            }
        }, OperationContext.Stage.RUNTIME);
    }
}
