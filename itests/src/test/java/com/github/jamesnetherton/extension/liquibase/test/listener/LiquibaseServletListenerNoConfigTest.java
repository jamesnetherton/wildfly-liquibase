/*-
 * #%L
 * wildfly-liquibase-itests
 * %%
 * Copyright (C) 2017 - 2019 James Netherton
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
package com.github.jamesnetherton.extension.liquibase.test.listener;

import com.github.jamesnetherton.liquibase.arquillian.LiquibaseTestSupport;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class LiquibaseServletListenerNoConfigTest extends LiquibaseTestSupport {

    private static final String NO_CONFIG_WEB_XML = "liquibase-servlet-listener-no-config-test.war";

    @ArquillianResource
    private Deployer deployer;

    @Deployment(managed = false, name = NO_CONFIG_WEB_XML)
    public static Archive<?> deployment() {
        return ShrinkWrap.create(WebArchive.class, NO_CONFIG_WEB_XML)
            .addAsWebInfResource("listener/web-empty.xml", "web.xml");
    }

    @Test
    public void testLiquibaseServletListenerNoConfig() {
        try {
            deployer.deploy(NO_CONFIG_WEB_XML);
            deployer.undeploy(NO_CONFIG_WEB_XML);
        } catch (Exception e){
            Assert.fail("Expected deployment to be successful but it failed: " + e.getMessage());
        }
    }
}
