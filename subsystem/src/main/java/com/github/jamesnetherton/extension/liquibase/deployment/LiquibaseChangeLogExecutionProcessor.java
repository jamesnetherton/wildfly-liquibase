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
import java.util.ArrayList;
import java.util.List;

import com.github.jamesnetherton.extension.liquibase.ChangeLogConfiguration;
import com.github.jamesnetherton.extension.liquibase.ChangeLogParserFactory;
import com.github.jamesnetherton.extension.liquibase.LiquibaseConstants;
import com.github.jamesnetherton.extension.liquibase.ModelConstants;
import com.github.jamesnetherton.extension.liquibase.service.ChangeLogExecutionService;

import org.jboss.as.connector.subsystems.datasources.AbstractDataSourceService;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.modules.Module;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;
import org.jboss.vfs.VirtualFile;

/**
 * {@link DeploymentUnitProcessor} which adds a {@link ChangeLogExecutionService} service dependency for
 * the deployment unit.
 */
public class LiquibaseChangeLogExecutionProcessor implements DeploymentUnitProcessor{

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {

        DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();

        List<VirtualFile> changeLogFiles = deploymentUnit.getAttachmentList(LiquibaseConstants.LIQUIBASE_CHANGELOGS);
        if (changeLogFiles.isEmpty()) {
            return;
        }

        Module module = deploymentUnit.getAttachment(Attachments.MODULE);

        List<ChangeLogConfiguration> changeLogConfigurations = new ArrayList<>();
        try {
            for (VirtualFile virtualFile : changeLogFiles) {
                File file = virtualFile.getPhysicalFile();
                String changeLogDefinition = new String(Files.readAllBytes(file.toPath()), "UTF-8");
                String datasourceRef = parseDataSourceRef(file, deploymentUnit.getName(), module.getClassLoader());

                ChangeLogConfiguration configuration = ChangeLogConfiguration.builder()
                    .name(file.getName())
                    .definition(changeLogDefinition)
                    .datasourceRef(datasourceRef)
                    .classLoader(module.getClassLoader())
                    .build();

                changeLogConfigurations.add(configuration);
            }
        } catch (IOException e) {
            throw new DeploymentUnitProcessingException(e);
        }

        ServiceName serviceName = ChangeLogExecutionService.createServiceName(deploymentUnit.getName());
        ChangeLogExecutionService service = new ChangeLogExecutionService(changeLogConfigurations);
        ServiceBuilder<ChangeLogExecutionService> builder = phaseContext.getServiceTarget().addService(serviceName, service);

        for (ChangeLogConfiguration configuration : changeLogConfigurations) {
            ContextNames.BindInfo bindInfo = ContextNames.bindInfoFor(configuration.getDatasourceRef());
            ServiceName dataSourceServiceName = AbstractDataSourceService.getServiceName(bindInfo);
            builder.addDependency(dataSourceServiceName);
        }

        builder.install();
    }

    @Override
    public void undeploy(DeploymentUnit deploymentUnit) {
    }

    private String parseDataSourceRef(File file, String runtimeName, ClassLoader classLoader) throws DeploymentUnitProcessingException {
        ChangeLogParser parser = ChangeLogParserFactory.createParser(file.getName());
        if (parser == null) {
            parser = ChangeLogParserFactory.createParser(runtimeName);
        }

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
