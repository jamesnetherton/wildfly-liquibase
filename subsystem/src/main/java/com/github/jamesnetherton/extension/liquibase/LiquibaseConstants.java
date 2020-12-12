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

import com.github.jamesnetherton.extension.liquibase.ChangeLogConfiguration.BuilderCollection;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.AttachmentList;

public interface LiquibaseConstants {

    /**
     * AttachmentList containing {@link ChangeLogConfiguration} instances
     */
    AttachmentKey<AttachmentList<ChangeLogConfiguration>> LIQUIBASE_CHANGELOGS = AttachmentKey.createList(ChangeLogConfiguration.class);

    /**
     * Attachment containing a collection {@link ChangeLogConfiguration.Builder} instances
     */
    AttachmentKey<BuilderCollection> LIQUIBASE_CHANGELOG_BUILDERS = AttachmentKey.create(BuilderCollection.class);

    /**
     * Attachment denoting whether the Liquibase subsystem was activated for a given deployment
     */
    AttachmentKey<Boolean> LIQUIBASE_SUBSYTEM_ACTIVATED = AttachmentKey.create(Boolean.class);

    /**
     * Liquibase change log file pattern
     */
    String LIQUIBASE_CHANGELOG_PATTERN = ".*changelog.(json|sql|xml|yaml|yml)$";
}
