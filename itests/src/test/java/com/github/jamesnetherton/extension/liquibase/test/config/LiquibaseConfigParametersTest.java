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
package com.github.jamesnetherton.extension.liquibase.test.config;

import com.github.jamesnetherton.liquibase.arquillian.LiquibaseTestSupport;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class LiquibaseConfigParametersTest extends LiquibaseTestSupport {

    @ArquillianResource
    private Deployer deployer;

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(JavaArchive.class, "liquibase-config-parameters-test.jar");
    }

    @Deployment(testable = false, managed = false, name = "changelog.jar")
    public static Archive<?> changelogDeployment() {
        return ShrinkWrap.create(JavaArchive.class, "changelog.jar")
            .addAsResource("configs/parameterized/changelog.xml", "parameterized-changelog.xml");
    }

    @Test
    public void testParameterizedChangeLog() throws Exception {
        try {
            System.setProperty("table.name", "param_testing");
            System.setProperty("changeset.name","param-testing");
            deployer.deploy("changelog.jar");
            assertTableModified("param_testing");
        } finally {
            System.clearProperty("table.name");
            System.clearProperty("changeset.name");
            deployer.undeploy("changelog.jar");
        }
    }
}
