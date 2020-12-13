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

import com.github.jamesnetherton.liquibase.arquillian.LiquibaseTestSupport;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.naming.InitialContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class LiquibaseConcurrentDeploymentTest extends LiquibaseTestSupport {

    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");
    private static final Map<String, String> DATASOURCES = new HashMap() {{
        put("TestDS1", "testdb1");
        put("TestDS2", "testdb2");
        put("TestDS3", "testdb3");
    }};

    @ArquillianResource
    private InitialContext context;

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(JavaArchive.class, "concurrent-deployment-test.jar")
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    public static Archive<?> concurrentDeployment1() {
        return ShrinkWrap.create(WebArchive.class, "concurrent-deployment-1-test.war")
            .addAsResource("deployment/concurrent/changelog-1.xml", "changelog.xml");
    }

    public static Archive<?> concurrentDeployment2() {
        return ShrinkWrap.create(WebArchive.class, "concurrent-deployment-2-test.war")
            .addAsResource("deployment/concurrent/changelog-2.xml", "changelog.xml");
    }

    public static Archive<?> concurrentDeployment3() {
        return ShrinkWrap.create(WebArchive.class, "concurrent-deployment-3-test.war")
            .addAsResource("deployment/concurrent/changelog-3.xml", "changelog.xml");
    }

    @BeforeClass
    public static void beforeClass() {
        Archive[] archives = new Archive[] {concurrentDeployment1(), concurrentDeployment2(), concurrentDeployment3()};
        for (Archive<?> archive : archives) {
            File deployment = Paths.get(TMP_DIR).resolve(archive.getName()).toFile();
            archive.as(ZipExporter.class).exportTo(deployment, true);
        }
    }

    @Before
    public void setUp() throws Exception {
        for (Map.Entry<String, String> entry : DATASOURCES.entrySet()) {
            addDataSource(entry.getKey(), entry.getValue());
        }
    }

    @After
    public void tearDown() {
        try {
            for (Map.Entry<String, String> entry : DATASOURCES.entrySet()) {
                removeDataSource(entry.getKey());
            }
        } catch (Exception e) {
            LOG.warn("Failed to remove datasource: {}", e);
        }
    }

    @Test
    public void testConcurrentDeployments() throws Exception {
        List<String> deployments = new ArrayList<>();
        ManagedExecutorService executor = (ManagedExecutorService) context.lookup("java:jboss/ee/concurrency/executor/default");
        CountDownLatch latch = new CountDownLatch(3);

        for (int i = 1; i <= 3; i++) {
            String deploymentName = String.format("concurrent-deployment-%d-test.war", i);
            File deployment = Paths.get(TMP_DIR).resolve(deploymentName).toFile();

            executor.submit(() -> {
                try {
                    deploy(deployment.getAbsolutePath());
                    deployments.add(deploymentName);
                } catch (Exception e) {
                    LOG.error("Failed to deploy {}: {}", deployment.getName(), e);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            Assert.assertTrue("Gave up waiting for deployments", latch.await(30, TimeUnit.SECONDS));

            for (Map.Entry<String, String> entry : DATASOURCES.entrySet()) {
                assertTableModified("table_" + entry.getValue(), DEFAULT_COLUMNS, "java:jboss/datasources/" + entry.getKey());
            }
        } finally {
            for (String deployment : deployments) {
                undeploy(deployment);
            }
        }
    }
}
