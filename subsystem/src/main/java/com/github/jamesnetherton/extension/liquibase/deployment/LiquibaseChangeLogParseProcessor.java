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

import java.io.IOException;

import com.github.jamesnetherton.extension.liquibase.ChangeLogConfiguration;
import com.github.jamesnetherton.extension.liquibase.LiquibaseConstants;
import com.github.jamesnetherton.extension.liquibase.LiquibaseLogger;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;

/**
 * {@link DeploymentUnitProcessor} which discovers Liquibase change log files within the deployment, reads their contents
 * and adds a {@link ChangeLogConfiguration} to the current deployment unit attachment list.
 */
public class LiquibaseChangeLogParseProcessor implements DeploymentUnitProcessor{

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {

        DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        if (deploymentUnit.getName().endsWith(".ear")) {
            return;
        }

        try {
            if (deploymentUnit.getName().matches(LiquibaseConstants.LIQUIBASE_CHANGELOG_PATTERN)) {
                VirtualFile virtualFile = deploymentUnit.getAttachment(Attachments.DEPLOYMENT_CONTENTS);
                LiquibaseLogger.ROOT_LOGGER.info("Found Liquibase changelog: {}", virtualFile.getName());
                deploymentUnit.addToAttachmentList(LiquibaseConstants.LIQUIBASE_CHANGELOGS, virtualFile);
            } else {
                VirtualFileFilter filter = new VirtualFileFilter() {
                    public boolean accepts(VirtualFile child) {
                        return child.isFile() && child.getName().matches(LiquibaseConstants.LIQUIBASE_CHANGELOG_PATTERN);
                    }
                };

                VirtualFile rootFile = deploymentUnit.getAttachment(Attachments.DEPLOYMENT_ROOT).getRoot();
                for (VirtualFile virtualFile : rootFile.getChildrenRecursively(filter)) {
                    LiquibaseLogger.ROOT_LOGGER.info("Found Liquibase changelog: {}", virtualFile.getName());
                    deploymentUnit.addToAttachmentList(LiquibaseConstants.LIQUIBASE_CHANGELOGS, virtualFile);
                }
            }
        } catch (IOException e) {
            throw new DeploymentUnitProcessingException(e);
        }
    }

    @Override
    public void undeploy(DeploymentUnit deploymentUnit) {
    }
}
