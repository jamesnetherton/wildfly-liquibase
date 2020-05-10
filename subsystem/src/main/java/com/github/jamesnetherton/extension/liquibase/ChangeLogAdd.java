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

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.registry.Resource;
import org.jboss.dmr.ModelNode;

final class ChangeLogAdd extends AbstractAddStepHandler {

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        ChangeLogResource.VALUE.validateAndSet(operation, model);
        ChangeLogResource.DATASOURCE.validateAndSet(operation, model);
        ChangeLogResource.CONTEXTS.validateAndSet(operation, model);
        ChangeLogResource.LABELS.validateAndSet(operation, model);
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        ChangeLogModelService service = ServiceHelper.getChangeLogModelUpdateService(context);
        service.createChangeLogModel(context, operation, model);
    }

    @Override
    protected void rollbackRuntime(OperationContext context, ModelNode operation, Resource resource) {
        super.rollbackRuntime(context, operation, resource);
        ChangeLogModelService service = ServiceHelper.getChangeLogModelUpdateService(context);
        try {
            service.removeChangeLogModel(context, operation);
        } catch (OperationFailedException e) {
            throw new IllegalStateException(e);
        }
    }
}
