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

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.AttachmentList;
import org.jboss.vfs.VirtualFile;

public interface LiquibaseConstants {

    /**
     * AttachmentList containing the String representations of the Liquibase change log definition
     */
    AttachmentKey<AttachmentList<VirtualFile>> LIQUIBASE_CHANGELOGS = AttachmentKey.createList(VirtualFile.class);

    /**
     * Liquibase change log file pattern
     */
    String LIQUIBASE_CHANGELOG_PATTERN = ".*changelog.(json|xml|yaml|yml)$";
}
