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

import com.github.jamesnetherton.extension.liquibase.service.ChangeLogModelService;
import com.github.jamesnetherton.extension.liquibase.service.ServiceHelper;

import org.jboss.as.controller.AbstractWriteAttributeHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;

final class ChangeLogWrite extends AbstractWriteAttributeHandler<Object> {

    static final ChangeLogWrite INSTANCE = new ChangeLogWrite();

    private ChangeLogWrite() {
        super(ChangeLogResource.VALUE, ChangeLogResource.DATASOURCE, ChangeLogResource.CONTEXTS, ChangeLogResource.LABELS);
    }

    @Override
    protected boolean applyUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName, ModelNode resolvedValue, ModelNode currentValue,
            HandbackHolder<Object> handbackHolder) throws OperationFailedException {
         updateRuntime(context, operation, resolvedValue, currentValue);
        return false;
    }

    @Override
    protected void revertUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName, ModelNode valueToRestore,
            ModelNode valueToRevert, Object handback) throws OperationFailedException {
         updateRuntime(context, operation, valueToRestore, valueToRevert);
    }

    private void updateRuntime(OperationContext context, ModelNode operation, ModelNode future, ModelNode current) throws OperationFailedException {
        ChangeLogModelService service = ServiceHelper.getChangeLogModelUpdateService(context);
        service.updateChangeLogModel(context, operation);
    }
}
