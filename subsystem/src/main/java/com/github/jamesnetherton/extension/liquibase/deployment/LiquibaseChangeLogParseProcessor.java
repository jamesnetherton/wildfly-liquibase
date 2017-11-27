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

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.parser.ChangeLogParser;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.github.jamesnetherton.extension.liquibase.ChangeLogConfiguration;
import com.github.jamesnetherton.extension.liquibase.ChangeLogConfigurationFactory;
import com.github.jamesnetherton.extension.liquibase.ChangeLogParserFactory;
import com.github.jamesnetherton.extension.liquibase.LiquibaseConstants;
import com.github.jamesnetherton.extension.liquibase.LiquibaseLogger;
import com.github.jamesnetherton.extension.liquibase.ModelConstants;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.modules.Module;
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
                addChangeLogAttachment(deploymentUnit, virtualFile.getPhysicalFile());
            } else {
                VirtualFileFilter filter = new VirtualFileFilter() {
                    public boolean accepts(VirtualFile child) {
                        return child.isFile() && child.getName().matches(LiquibaseConstants.LIQUIBASE_CHANGELOG_PATTERN);
                    }
                };

                VirtualFile rootFile = deploymentUnit.getAttachment(Attachments.DEPLOYMENT_ROOT).getRoot();
                for (VirtualFile virtualFile : rootFile.getChildrenRecursively(filter)) {
                    LiquibaseLogger.ROOT_LOGGER.info("Found Liquibase changelog: {}", virtualFile.getName());
                    addChangeLogAttachment(deploymentUnit, virtualFile.getPhysicalFile());
                }
            }
        } catch (IOException e) {
            throw new DeploymentUnitProcessingException(e);
        }
    }

    @Override
    public void undeploy(DeploymentUnit deploymentUnit) {
    }

    private void addChangeLogAttachment(DeploymentUnit deploymentUnit, File file) throws DeploymentUnitProcessingException, IOException {
        Module module = deploymentUnit.getAttachment(Attachments.MODULE);
        String changeLogDefinition = new String(Files.readAllBytes(file.toPath()), "UTF-8");
        String datasourceRef = parseDataSourceRef(file, module.getClassLoader());

        ChangeLogConfiguration configuration = ChangeLogConfigurationFactory.createChangeLogConfiguration(file.getName(), changeLogDefinition, datasourceRef);
        deploymentUnit.addToAttachmentList(LiquibaseConstants.LIQUIBASE_CHANGELOGS, configuration);
    }

    private String parseDataSourceRef(File file, ClassLoader classLoader) throws DeploymentUnitProcessingException {
        ChangeLogParser parser = ChangeLogParserFactory.createFactory(file);
        if (parser == null) {
            throw new DeploymentUnitProcessingException("Unable to find a suitable change log parser for " + file.getName());
        }

        try {
            CompositeResourceAccessor resourceAccessor = new CompositeResourceAccessor(new FileSystemResourceAccessor(), new ClassLoaderResourceAccessor(classLoader));
            DatabaseChangeLog changeLog = parser.parse(file.getAbsolutePath(), new ChangeLogParameters(), resourceAccessor);
            Object datasourceRef = changeLog.getChangeLogParameters().getValue(ModelConstants.DATASOURCE_REF, changeLog);
            if (datasourceRef == null) {
                throw new DeploymentUnitProcessingException("Change log is missing a datasource-ref property");
            }
            return (String) datasourceRef;
        } catch (ChangeLogParseException e) {
            throw new DeploymentUnitProcessingException(e);
        }
    }
}
