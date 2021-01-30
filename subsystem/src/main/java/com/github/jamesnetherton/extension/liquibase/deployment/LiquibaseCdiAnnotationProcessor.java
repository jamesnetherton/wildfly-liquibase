/*-
 * #%L
 * wildfly-liquibase-itests
 * %%
 * Copyright (C) 2017 - 2019 James Netherton
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

import com.github.jamesnetherton.extension.liquibase.LiquibaseConstants;
import java.util.List;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.annotation.CompositeIndex;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;

/**
 * {@link DeploymentUnitProcessor} to discover classes within the deployment that use Liquibase CDI annotations.
 */
public class LiquibaseCdiAnnotationProcessor implements DeploymentUnitProcessor {

    private static final String LIQUIBASE_CDI_PACKAGE_BASE = "liquibase.integration.cdi.annotations";
    private static final DotName[] LIQUIBASE_CDI_ANNOTATIONS = new DotName[] {
        DotName.createSimple(LIQUIBASE_CDI_PACKAGE_BASE + ".Liquibase"),
        DotName.createSimple(LIQUIBASE_CDI_PACKAGE_BASE + "LiquibaseSchema"),
        DotName.createSimple(LIQUIBASE_CDI_PACKAGE_BASE + ".LiquibaseType")
    };

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();

        CompositeIndex index = deploymentUnit.getAttachment(Attachments.COMPOSITE_ANNOTATION_INDEX);
        if (liquibaseCdiAnnotationsPresent(index)) {
            deploymentUnit.putAttachment(LiquibaseConstants.LIQUIBASE_SUBSYTEM_ACTIVATED, Boolean.TRUE);
        }
    }

    @Override
    public void undeploy(DeploymentUnit deploymentUnit) {
    }

    private boolean liquibaseCdiAnnotationsPresent(CompositeIndex index) {
        for (DotName dotName : LIQUIBASE_CDI_ANNOTATIONS) {
            List<AnnotationInstance> annotations = index.getAnnotations(dotName);
            if (!annotations.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
