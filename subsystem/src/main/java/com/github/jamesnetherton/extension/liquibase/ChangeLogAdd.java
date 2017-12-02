package com.github.jamesnetherton.extension.liquibase;

import com.github.jamesnetherton.extension.liquibase.service.ChangeLogModelUpdateService;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.registry.Resource;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;

final class ChangeLogAdd extends AbstractAddStepHandler {

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        ChangeLogResource.VALUE.validateAndSet(operation, model);
        ChangeLogResource.DATASOURCE_REF.validateAndSet(operation, model);
        ChangeLogResource.CONTEXT_NAMES.validateAndSet(operation, model);
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        ServiceName serviceName = ChangeLogModelUpdateService.createServiceName();
        ServiceController<ChangeLogModelUpdateService> controller = (ServiceController<ChangeLogModelUpdateService>) context.getServiceRegistry(false).getService(serviceName);
        ChangeLogModelUpdateService service = controller.getValue();
        service.updateChangeLogModel(context, operation, model);
    }

    @Override
    protected void rollbackRuntime(OperationContext context, ModelNode operation, Resource resource) {
        super.rollbackRuntime(context, operation, resource);
    }
}
