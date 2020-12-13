package com.github.jamesnetherton.extension.liquibase.service;

import com.github.jamesnetherton.extension.liquibase.ChangeLogConfiguration;
import com.github.jamesnetherton.extension.liquibase.LiquibaseLogger;
import java.net.SocketException;
import java.net.UnknownHostException;
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
import liquibase.util.NetUtil;
import org.jboss.as.controller.OperationContext;
import org.jboss.msc.service.AbstractService;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;

public final class ServiceHelper {

    public static void installService(ServiceName serviceName, ServiceTarget serviceTarget, AbstractService<?> service) {
        ServiceBuilder<?> builder = serviceTarget.addService(serviceName, service);
        builder.install();
    }

    @SuppressWarnings("unchecked")
    public static <T> T getService(OperationContext context, ServiceName serviceName, Class<?> T) {
        ServiceController<T> controller = (ServiceController<T>) context.getServiceRegistry(false).getService(serviceName);
        return controller.getValue();
    }

    public static ChangeLogModelService getChangeLogModelUpdateService(OperationContext context) {
        ServiceName serviceName = ChangeLogModelService.getServiceName();
        return getService(context, serviceName, ChangeLogModelService.class);
    }

    public static boolean isChangeLogExecutable(ChangeLogConfiguration configuration) {
        final String hostExcludes = configuration.getHostExcludes();
        final String hostIncludes = configuration.getHostIncludes();

        if ((hostExcludes == null || hostExcludes.isEmpty()) && (hostIncludes == null || hostIncludes.isEmpty())) {
            return true;
        }

        try {
            String hostName = NetUtil.getLocalHostName();

            if (hostIncludes != null && !hostIncludes.isEmpty()) {
                for (String host : hostIncludes.split(",")) {
                    host = host.trim();
                    if (hostName.equalsIgnoreCase(host)) {
                        return true;
                    }
                }
            } else if (hostExcludes != null && !hostExcludes.isEmpty()) {
                for (String host : hostExcludes.split(",")) {
                    host = host.trim();
                    if (hostName.equalsIgnoreCase(host)) {
                        return false;
                    }
                }
                return true;
            }
        } catch (SocketException | UnknownHostException e) {
            LiquibaseLogger.ROOT_LOGGER.warn("Unable to process host-excludes or host-includes. Failed looking up hostname", e);
        }

        return false;
    }
}
