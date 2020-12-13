package com.github.jamesnetherton.extension.liquibase.test.scope;

/*-
 * #%L
 * wildfly-liquibase-itests
 * %%
 * Copyright (C) 2017 - 2020 James Netherton
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
import com.github.jamesnetherton.extension.liquibase.scope.WildFlyScopeManager;
import com.github.jamesnetherton.extension.liquibase.test.scope.producer.LiquibaseConfigurationProducer;
import com.github.jamesnetherton.liquibase.arquillian.ChangeLogDefinition;
import com.github.jamesnetherton.liquibase.arquillian.LiquibaseTestSupport;
import java.io.File;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class LiquibaseScopeManagerTest extends LiquibaseTestSupport {

    private static final String DEPLOYMENT_BASIC = "liquibase-scope-basic-test.war";
    private static final String DEPLOYMENT_CDI = "liquibase-scope-cdi-test.jar";
    private static final String DEPLOYMENT_JAR = "liquibase-scope-jar-test.jar";
    private static final String DEPLOYMENT_SERVLET_LISTENER = "liquibase-scope-servlet-listener-test.war";
    private static final String DEPLOYMENT_XML = "scope-test-changes-test.xml";

    @ArquillianResource
    private Deployer deployer;

    @ChangeLogDefinition(format = "xml", fileName = DEPLOYMENT_XML)
    private String tableNameXML;

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(JavaArchive.class, "liquibase-scope-manager-test.jar")
                .setManifest(new StringAsset("Manifest-Version: 1.0\n" +
                        "Dependencies: org.jboss.as.controller-client,com.github.jamesnetherton.extension.liquibase\n"));
    }

    @Deployment(managed = false, testable = false, name = DEPLOYMENT_BASIC)
    public static Archive<?> standardDeployment() {
        return ShrinkWrap.create(WebArchive.class, DEPLOYMENT_BASIC)
                .addAsWebInfResource("listener/web-empty.xml", "web.xml");
    }

    @Deployment(managed = false, testable = false, name = DEPLOYMENT_SERVLET_LISTENER)
    public static Archive<?> servletDeployment() {
        return ShrinkWrap.create(WebArchive.class, DEPLOYMENT_SERVLET_LISTENER)
                .addAsWebInfResource("listener/web.xml")
                .addAsResource("configs/scope/listener.xml", "liquibase/changelog.xml");
    }

    @Deployment(managed = false, testable = false, name = DEPLOYMENT_CDI)
    public static Archive<?> cdiDeployment() {
        return ShrinkWrap.create(JavaArchive.class, DEPLOYMENT_CDI)
                .addClass(LiquibaseConfigurationProducer.class)
                .addAsResource("configs/scope/cdi.xml", "/com/github/jamesnetherton/liquibase/test/changes.xml")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Deployment(managed = false, testable = false, name = DEPLOYMENT_JAR)
    public static Archive<?> jarDeployment() {
        return ShrinkWrap.create(JavaArchive.class, DEPLOYMENT_JAR)
                .addAsResource("configs/scope/simple.xml", "changelog-simple.xml");
    }

    @Test
    public void testScopeManagerContainsSingleEntry() throws Exception {
        Assert.assertEquals(1, WildFlyScopeManager.getScopes().size());

        String runtimeName = null;
        try {
            deployer.deploy(DEPLOYMENT_BASIC);
            deployer.deploy(DEPLOYMENT_CDI);
            deployer.deploy(DEPLOYMENT_JAR);
            deployer.deploy(DEPLOYMENT_SERVLET_LISTENER);

            runtimeName = deployChangeLog(DEPLOYMENT_XML, DEPLOYMENT_XML);

            boolean success = executeCliScript(new File("target/test-classes/cli/changelog-scope.cli"));
            Assert.assertTrue("Expected changelog-scope.cli success but it failed", success);

            // non-subsystem managed deployments have their scopes removed on undeploy so expect the count to be > 1 at this point
            Assert.assertEquals(3, WildFlyScopeManager.getScopes().size());
        } finally {
            deployer.undeploy(DEPLOYMENT_BASIC);
            deployer.undeploy(DEPLOYMENT_CDI);
            deployer.undeploy(DEPLOYMENT_JAR);
            deployer.undeploy(DEPLOYMENT_SERVLET_LISTENER);
            undeployChangeLog(runtimeName);
            removeLiquibaseDmrModel("dmr-scope-test.xml");
        }

        Assert.assertEquals(1, WildFlyScopeManager.getScopes().size());
    }

}
