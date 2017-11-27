/*-
 * #%L
 * wildfly-liquibase-itests
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
package com.github.jamesnetherton.extension.liquibase.test.dmr;

import java.io.File;
import java.util.Arrays;

import com.github.jamesnetherton.extension.liquibase.test.common.LiquibaseTestSupport;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class LiquibaseDmrModelTest extends LiquibaseTestSupport {

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(JavaArchive.class, "liquibase-dmr-model-test.jar")
            .addClass(LiquibaseTestSupport.class);
    }

    @Test
    public void testDmrModel() throws Exception {
        try {
            executeCliScript(new File("target/test-classes/cli/changelog-add.cli"));
            assertTableModified("dmr_add");
        } finally {
            executeCliScript(new File("target/test-classes/cli/changelog-remove.cli"));
        }
    }

    @Test
    public void testDmrModeWithContext() throws Exception {
        try {
            executeCliScript(new File("target/test-classes/cli/changelog-add-with-context.cli"));
            assertTableModified("dmr_add_with_context", Arrays.asList("firstname", "id", "lastname", "state"));
        } finally {
            executeCliScript(new File("target/test-classes/cli/changelog-remove-with-context.cli"));
        }
    }


}
