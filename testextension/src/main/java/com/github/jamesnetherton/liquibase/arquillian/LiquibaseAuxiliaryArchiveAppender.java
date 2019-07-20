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

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

public final class LiquibaseAuxiliaryArchiveAppender implements AuxiliaryArchiveAppender{

    @Override
    public Archive<?> createAuxiliaryArchive() {
        return ShrinkWrap.create(JavaArchive.class, "liquibase-extension.jar")
            // Add test support class
            .addClass(LiquibaseTestSupport.class)
            // Add extension classes
            .addPackage(LiquibaseLoadableExtension.class.getPackage())
            // Add loadable extension
            .addAsServiceProvider(RemoteLoadableExtension.class, LiquibaseRemoteLoadableExtension.class);
    }
}
