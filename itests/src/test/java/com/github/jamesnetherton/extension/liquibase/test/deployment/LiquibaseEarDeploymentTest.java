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
package com.github.jamesnetherton.extension.liquibase.test.deployment;

import javax.ejb.Stateless;

import com.github.jamesnetherton.liquibase.arquillian.LiquibaseTestSupport;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class LiquibaseEarDeploymentTest extends LiquibaseTestSupport {

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(JavaArchive.class, "liquibase-ear-deployment-test.jar");
    }

    @Deployment(name = "liquibase-ear.ear", testable = false)
    public static Archive<?> earDeployment() {
        return ShrinkWrap.create(EnterpriseArchive.class, "liquibase-ear-deployment.ear")
            .addAsModule(
                ShrinkWrap.create(JavaArchive.class, "liquibase-jar-subdeployment.jar")
                    .addClass(EjbBean.class)
                    .addAsResource("configs/simple/changelog.xml")
            );
    }

    @Test
    public void testEarDeployment() throws Exception {
        assertTableModified("ear_test");
    }

    @Stateless
    private static class EjbBean {
    }
}
