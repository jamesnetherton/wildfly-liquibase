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

import org.jboss.as.ee.weld.WeldDeploymentMarker;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.annotation.CompositeIndex;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.as.server.moduleservice.ServiceModuleLoader;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.modules.ModuleIdentifier;

/**
 * {@link DeploymentUnitProcessor} which adds Liquibase module dependencies to the deployment
 */
public class LiquibaseDependenciesProcessor implements DeploymentUnitProcessor{

    private static final String MODULE_LIQUIBASE_CORE = "org.liquibase.core";
    private static final String MODULE_LIQUIBASE_CDI = "org.liquibase.cdi";
    private static final String LIQUIBASE_TYPE = "liquibase.integration.cdi.annotations.LiquibaseType";

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {

        DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();

        ServiceModuleLoader moduleLoader = deploymentUnit.getAttachment(Attachments.SERVICE_MODULE_LOADER);
        ModuleSpecification moduleSpec = deploymentUnit.getAttachment(Attachments.MODULE_SPECIFICATION);

        ModuleIdentifier liquibaseCore = ModuleIdentifier.fromString(MODULE_LIQUIBASE_CORE);
        moduleSpec.addUserDependency(new ModuleDependency(moduleLoader, liquibaseCore, false, true, true, true));

        CompositeIndex index = deploymentUnit.getAttachment(Attachments.COMPOSITE_ANNOTATION_INDEX);
        List<AnnotationInstance> annotations = index.getAnnotations(DotName.createSimple(LIQUIBASE_TYPE));

        if (WeldDeploymentMarker.isPartOfWeldDeployment(deploymentUnit) && !annotations.isEmpty()) {
            ModuleIdentifier liquibaseCdi = ModuleIdentifier.fromString(MODULE_LIQUIBASE_CDI);
            moduleSpec.addUserDependency(new ModuleDependency(moduleLoader, liquibaseCdi, false, true, true, true));
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {
    }
}
