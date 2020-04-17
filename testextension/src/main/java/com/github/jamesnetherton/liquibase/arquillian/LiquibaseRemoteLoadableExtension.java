/*-
 * #%L
 * wildfly-liquibase-testextension
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
package com.github.jamesnetherton.liquibase.arquillian;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.as.arquillian.service.DependenciesProvider;
import org.jboss.modules.ModuleIdentifier;

public final class LiquibaseRemoteLoadableExtension implements RemoteLoadableExtension, DependenciesProvider {

    private static final Set<ModuleIdentifier> DEPENDENCIES = new LinkedHashSet<>();
    static {
        DEPENDENCIES.add(ModuleIdentifier.create("com.github.jamesnetherton.extension.liquibase"));
        DEPENDENCIES.add(ModuleIdentifier.create("org.liquibase.core"));
    }

    @Override
    public void register(ExtensionBuilder builder) {
        builder.service(TestEnricher.class, ChangeLogDefinitionEnricher.class);
    }

    @Override
    public Set<ModuleIdentifier> getDependencies() {
        return Collections.unmodifiableSet(DEPENDENCIES);
    }
}
