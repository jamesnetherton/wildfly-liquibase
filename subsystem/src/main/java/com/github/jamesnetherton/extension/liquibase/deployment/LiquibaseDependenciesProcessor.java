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

import com.github.jamesnetherton.extension.liquibase.LiquibaseConstants;
import com.github.jamesnetherton.extension.liquibase.service.ChangeLogExecutionService;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.as.server.moduleservice.ServiceModuleLoader;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.vfs.VirtualFile;

/**
 * {@link DeploymentUnitProcessor} which adds Liquibase module dependencies to the deployment
 */
public class LiquibaseDependenciesProcessor implements DeploymentUnitProcessor{

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {

        DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();

        List<VirtualFile> changeLogFiles = deploymentUnit.getAttachmentList(LiquibaseConstants.LIQUIBASE_CHANGELOGS);
        if (changeLogFiles.isEmpty()) {
            return;
        }

        ServiceModuleLoader moduleLoader = deploymentUnit.getAttachment(Attachments.SERVICE_MODULE_LOADER);
        ModuleSpecification moduleSpec = deploymentUnit.getAttachment(Attachments.MODULE_SPECIFICATION);
        ModuleIdentifier identifier = ModuleIdentifier.fromString("org.liquibase");
        moduleSpec.addUserDependency(new ModuleDependency(moduleLoader, identifier, false, true, true, true));
    }

    @Override
    public void undeploy(DeploymentUnit context) {
    }
}
