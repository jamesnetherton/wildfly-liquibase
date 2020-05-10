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
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.jamesnetherton.extension.liquibase.ChangeLogConfiguration;
import com.github.jamesnetherton.extension.liquibase.ChangeLogConfiguration.Builder;
import com.github.jamesnetherton.extension.liquibase.ChangeLogConfiguration.BuilderCollection;
import com.github.jamesnetherton.extension.liquibase.ChangeLogParserFactory;
import com.github.jamesnetherton.extension.liquibase.LiquibaseConstants;
import com.github.jamesnetherton.extension.liquibase.LiquibaseLogger;
import com.github.jamesnetherton.extension.liquibase.ModelConstants;
import com.github.jamesnetherton.extension.liquibase.resource.VFSResourceAccessor;

import org.jboss.as.ee.structure.DeploymentType;
import org.jboss.as.ee.structure.DeploymentTypeMarker;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.web.common.WarMetaData;
import org.jboss.metadata.javaee.spec.ParamValueMetaData;
import org.jboss.metadata.web.spec.WebMetaData;
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
        if (DeploymentTypeMarker.isType(DeploymentType.EAR, deploymentUnit)) {
            return;
        }

        Optional<String> changeLogContextParam = Optional.empty();
        WarMetaData warMetaData = deploymentUnit.getAttachment(WarMetaData.ATTACHMENT_KEY);
        if (warMetaData != null) {
            WebMetaData webMetaData = warMetaData.getWebMetaData();
            if (webMetaData != null) {
                changeLogContextParam = webMetaData.getContextParams()
                    .stream()
                    .filter(paramValueMetaData -> paramValueMetaData.getParamName().equals("liquibase.changelog"))
                    .map(ParamValueMetaData::getParamValue)
                    .findFirst();
            }
        }

        Module module = deploymentUnit.getAttachment(Attachments.MODULE);
        List<VirtualFile> changeLogFiles = new ArrayList<>();

        try {
            if (deploymentUnit.getName().matches(LiquibaseConstants.LIQUIBASE_CHANGELOG_PATTERN)) {
                VirtualFile virtualFile = deploymentUnit.getAttachment(Attachments.DEPLOYMENT_CONTENTS);
                LiquibaseLogger.ROOT_LOGGER.info("Found Liquibase changelog: {}", virtualFile.getName());
                changeLogFiles.add(virtualFile);
            } else {
                String contextParam = changeLogContextParam.orElse("/");
                VirtualFileFilter filter = file -> file.isFile() && file.getName().matches(LiquibaseConstants.LIQUIBASE_CHANGELOG_PATTERN) && !file.getPathName().endsWith(contextParam);
                VirtualFile rootFile = deploymentUnit.getAttachment(Attachments.DEPLOYMENT_ROOT).getRoot();
                for (VirtualFile virtualFile : rootFile.getChildrenRecursively(filter)) {
                    LiquibaseLogger.ROOT_LOGGER.info("Found Liquibase changelog: {}", virtualFile.getName());
                    changeLogFiles.add(virtualFile);
                }
            }

            BuilderCollection builderCollection = deploymentUnit.getAttachment(LiquibaseConstants.LIQUIBASE_CHANGELOG_BUILDERS);

            for (VirtualFile virtualFile : changeLogFiles) {
                File file = virtualFile.getPhysicalFile();
                String changeLogDefinition = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                String dataSource = parseDataSource(virtualFile, deploymentUnit.getName(), module.getClassLoader());

                Builder builder;
                if (builderCollection == null) {
                    builder = ChangeLogConfiguration.builder();
                } else {
                    builder = builderCollection.getOrCreateBuilder(file.getName());
                }

                ChangeLogConfiguration configuration = builder.name(file.getName())
                    .path(virtualFile.getPathName())
                    .deployment(deploymentUnit.getName())
                    .definition(changeLogDefinition)
                    .dataSource(dataSource)
                    .classLoader(module.getClassLoader())
                    .deploymentOrigin()
                    .build();

                deploymentUnit.addToAttachmentList(LiquibaseConstants.LIQUIBASE_CHANGELOGS, configuration);
            }
        } catch (IOException e) {
            throw new DeploymentUnitProcessingException(e);
        }
    }

    @Override
    public void undeploy(DeploymentUnit deploymentUnit) {
    }

    private String parseDataSource(VirtualFile file, String runtimeName, ClassLoader classLoader) throws DeploymentUnitProcessingException {
        ChangeLogParser parser = ChangeLogParserFactory.createParser(file.getName());
        if (parser == null) {
            parser = ChangeLogParserFactory.createParser(runtimeName);
        }

        if (parser == null) {
            throw new DeploymentUnitProcessingException("Unable to find a suitable change log parser for " + file.getName());
        }

        try {
            //TODO: Refactor this. Creating ChangeLogConfiguration just to satisfy VFSResourceAccessor is wasteful
            ChangeLogConfiguration configuration = new ChangeLogConfiguration();
            configuration.setName(file.getName());
            configuration.setPath(file.getPathName());
            configuration.setDeployment(runtimeName);
            configuration.setClassLoader(classLoader);

            CompositeResourceAccessor resourceAccessor = new CompositeResourceAccessor(new FileSystemResourceAccessor(), new VFSResourceAccessor(configuration));
            DatabaseChangeLog changeLog = parser.parse(file.getPhysicalFile().getAbsolutePath(), new ChangeLogParameters(), resourceAccessor);
            Object dataSource = changeLog.getChangeLogParameters().getValue(ModelConstants.DATASOURCE, changeLog);
            if (dataSource == null) {
                throw new DeploymentUnitProcessingException("Change log is missing a datasource property");
            }
            return (String) dataSource;
        } catch (ChangeLogParseException | IOException e) {
            throw new DeploymentUnitProcessingException(e);
        }
    }

}
