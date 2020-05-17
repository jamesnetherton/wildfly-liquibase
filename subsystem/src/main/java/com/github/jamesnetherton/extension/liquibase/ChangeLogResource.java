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

import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelType;

public final class ChangeLogResource extends SimpleResourceDefinition {

    public static final PathElement CHANGE_LOG_PATH = PathElement.pathElement(ModelConstants.DATABASE_CHANGELOG);

    public static final SimpleAttributeDefinition CONTEXTS = new SimpleAttributeDefinitionBuilder(ModelConstants.CONTEXTS, ModelType.STRING)
        .addFlag(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES)
        .setAllowExpression(true)
        .setRequired(false)
        .build();

    public static final SimpleAttributeDefinition DATASOURCE = new SimpleAttributeDefinitionBuilder(ModelConstants.DATASOURCE, ModelType.STRING)
        .addFlag(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES)
        .setAllowExpression(true)
        .build();

    public static final SimpleAttributeDefinition FAIL_ON_ERROR = new SimpleAttributeDefinitionBuilder(ModelConstants.FAIL_ON_ERROR, ModelType.BOOLEAN)
        .addFlag(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES)
        .setAllowExpression(true)
        .setRequired(false)
        .build();

    public static final SimpleAttributeDefinition HOST_EXCLUDES = new SimpleAttributeDefinitionBuilder(ModelConstants.HOST_EXCLUDES, ModelType.STRING)
        .addFlag(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES)
        .setAllowExpression(true)
        .setRequired(false)
        .build();

    public static final SimpleAttributeDefinition HOST_INCLUDES = new SimpleAttributeDefinitionBuilder(ModelConstants.HOST_INCLUDES, ModelType.STRING)
        .addFlag(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES)
        .setAllowExpression(true)
        .setRequired(false)
        .build();

    public static final SimpleAttributeDefinition LABELS = new SimpleAttributeDefinitionBuilder(ModelConstants.LABELS, ModelType.STRING)
        .addFlag(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES)
        .setAllowExpression(true)
        .setRequired(false)
        .build();

    public static final SimpleAttributeDefinition VALUE = new SimpleAttributeDefinitionBuilder(ModelConstants.VALUE, ModelType.STRING)
        .addFlag(AttributeAccess.Flag.RESTART_NONE)
        .setAllowExpression(false)
        .build();

    ChangeLogResource() {
        super(CHANGE_LOG_PATH,
          LiquibaseResourceDescriptionResolvers.getResolver(ModelConstants.DATABASE_CHANGELOG),
          new ChangeLogAdd(),
          new ChangeLogRemove()
        );
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerReadWriteAttribute(CONTEXTS, null, ChangeLogWrite.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(DATASOURCE, null, ChangeLogWrite.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(FAIL_ON_ERROR, null, ChangeLogWrite.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(HOST_EXCLUDES, null, ChangeLogWrite.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(HOST_INCLUDES, null, ChangeLogWrite.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(LABELS, null, ChangeLogWrite.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(VALUE, null, ChangeLogWrite.INSTANCE);
    }
}
