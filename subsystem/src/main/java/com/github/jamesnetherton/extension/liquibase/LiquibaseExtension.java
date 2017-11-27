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

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.ModelVersion;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.parsing.ExtensionParsingContext;

public final class LiquibaseExtension implements Extension {

    public static final String SUBSYSTEM_NAME = "liquibase";

    private static final int API_MAJOR_VERSION = 1;
    private static final int API_MINOR_VERSION = 0;
    private static final int API_MICRO_VERSION = 0;


    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(SUBSYSTEM_NAME, Namespace.VERSION_1_0.getUriString(), LiquibaseSubsystemParser.INSTANCE);
    }

    @Override
    public void initialize(ExtensionContext context) {
        ModelVersion modelVersion = ModelVersion.create(API_MAJOR_VERSION, API_MINOR_VERSION, API_MICRO_VERSION);
        SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME, modelVersion);
        subsystem.registerSubsystemModel(new LiquibaseRootResource());
        subsystem.registerXMLElementWriter(LiquibaseSubsystemWriter.INSTANCE);
    }

}
