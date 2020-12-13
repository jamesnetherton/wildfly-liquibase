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

import com.github.jamesnetherton.liquibase.arquillian.LiquibaseTestSupport;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import liquibase.util.NetUtil;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class LiquibaseDmrModelTest extends LiquibaseTestSupport {

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(JavaArchive.class, "liquibase-dmr-model-test.jar")
            .addAsManifestResource("configs/ds/liquibase-example-ds.xml", "liquibase-example-ds.xml");
    }

    @Test
    public void testDmrModelCreate() throws Exception {
        try {
            boolean success = executeCliScript(new File("target/test-classes/cli/changelog-add.cli"));
            Assert.assertTrue("Expected changelog-add.cli success but it failed", success);
            assertTableModified("dmr_add");
        } finally {
            removeLiquibaseDmrModel("dmr-model-test.xml");
        }
    }

    @Test
    public void testDmrModelCreateWithContext() throws Exception {
        try {
            boolean success = executeCliScript(new File("target/test-classes/cli/changelog-add-with-context.cli"));
            Assert.assertTrue("Expected changelog-add-with-context.cli success but it failed", success);
            assertTableModified("dmr_add_with_context", Arrays.asList("firstname", "id", "lastname", "state"));
        } finally {
            removeLiquibaseDmrModel("dmr-model-with-context-test.xml");
        }
    }

    @Test
    public void testDmrModelCreateWithLabel() throws Exception {
        try {
            boolean success = executeCliScript(new File("target/test-classes/cli/changelog-add-with-label.cli"));
            Assert.assertTrue("Expected changelog-add-with-label.cli success but it failed", success);
            assertTableModified("dmr_add_with_label", Arrays.asList("firstname", "id", "lastname", "state"));
        } finally {
            removeLiquibaseDmrModel("dmr-model-with-label-test.xml");
        }
    }

    @Test
    public void testDmrModelCreateWithFailOnError() throws Exception {
        try {
            boolean success = executeCliScript(new File("target/test-classes/cli/changelog-add-with-fail-on-error.cli"));
            Assert.assertTrue("Expected changelog-add-with-fail-on-error.cli success but it failed", success);
            assertTableModified("dmr_add_with_fail_on_error");
        } finally {
            removeLiquibaseDmrModel("dmr-model-with-fail-on-error-test.xml");
        }
    }

    @Test
    public void testDmrModelCreateWithHostExcludes() throws Exception {
        try {
            System.setProperty("host.excludes", NetUtil.getLocalHostName());
            boolean success = executeCliScript(new File("target/test-classes/cli/changelog-add-with-host-excludes.cli"));
            Assert.assertTrue("Expected changelog-add-with-host-excludes.cli success but it failed", success);
            assertTableModified("dmr_add_with_host_excludes", Collections.emptyList());
        } finally {
            removeLiquibaseDmrModel("dmr-model-with-host-excludes-test.xml");
            System.clearProperty("host.excludes");
        }
    }

    @Test
    public void testDmrModelCreateWithHostIncludes() throws Exception {
        try {
            boolean success = executeCliScript(new File("target/test-classes/cli/changelog-add-with-host-includes.cli"));
            Assert.assertTrue("Expected changelog-add-with-host-includes.cli success but it failed", success);
            assertTableModified("dmr_add_with_host_includes", Collections.emptyList());
        } finally {
            removeLiquibaseDmrModel("dmr-model-with-host-includes-test.xml");
        }
    }

    @Test
    public void testDmrModelCreateWithNoFileNameExtension() throws Exception {
        try {
            boolean success = executeCliScript(new File("target/test-classes/cli/changelog-add-with-no-filename-extension.cli"));
            Assert.assertTrue("Expected changelog-add-with-no-filename-extension.cli success but it failed", success);
            assertTableModified("dmr_add_name_with_no_filename_extension");
        } finally {
            removeLiquibaseDmrModel("dmr-model-with-no-file-extension");
        }
    }

    @Test
    public void testDmrModelCreateWithDatasourcePlaceholder() throws Exception {
        try {
            System.setProperty("datasource.name", "java:jboss/datasources/ExampleDS");
            boolean success = executeCliScript(new File("target/test-classes/cli/changelog-add-with-datasource-placeholder.cli"));
            Assert.assertTrue("Expected changelog-add-with-datasource-placeholder.cli success but it failed", success);
            assertTableModified("dmr_add_name_with_datasource_ref_placeholder");
        } finally {
            removeLiquibaseDmrModel("dmr-model-with-datasource-placeholder.xml");
            System.clearProperty("datasource.name");
        }
    }

    @Test
    public void testDmrModelCreateWithContextPlaceholder() throws Exception {
        try {
            System.setProperty("context.name", "context-placeholder-test");
            boolean success = executeCliScript(new File("target/test-classes/cli/changelog-add-with-context-placeholder.cli"));
            Assert.assertTrue("Expected changelog-add-with-context-placeholder.cli success but it failed", success);
            assertTableModified("dmr_add_with_context", Arrays.asList("firstname", "id", "lastname", "state"));
        } finally {
            removeLiquibaseDmrModel("dmr-model-with-context-placeholder.xml");
            System.clearProperty("context.name");
        }
    }

    @Test
    public void testDmrModelCreateWithLabelPlaceholder() throws Exception {
        try {
            System.setProperty("label.name", "label-placeholder-test");
            boolean success = executeCliScript(new File("target/test-classes/cli/changelog-add-with-label-placeholder.cli"));
            Assert.assertTrue("Expected changelog-add-with-label-placeholder.cli success but it failed", success);
            assertTableModified("dmr_add_with_label", Arrays.asList("firstname", "id", "lastname", "state"));
        } finally {
            removeLiquibaseDmrModel("dmr-model-with-label-placeholder.xml");
            System.clearProperty("label.name");
        }
    }

    @Test
    public void testDmrModelCreateWithFailOnErrorPlaceholder() throws Exception {
        try {
            System.setProperty("do.fail.on.error", "false");
            boolean success = executeCliScript(new File("target/test-classes/cli/changelog-add-with-fail-on-error-placeholder.cli"));
            Assert.assertTrue("Expected changelog-add-with-fail-on-error-placeholder.cli success but it failed", success);
            assertTableModified("dmr_add_with_fail_on_error_placeholder");
        } finally {
            removeLiquibaseDmrModel("dmr-model-with-fail-on-error-placeholder.xml");
            System.clearProperty("do.fail.on.error");
        }
    }

    @Test
    public void testDmrModelCreateWithDuplicateDatasource() throws Exception {
        boolean success = executeCliScript(new File("target/test-classes/cli/changelog-add-with-duplicate-datasource.cli"));
        Assert.assertFalse("Expected changelog-add-with-duplicate-datasource.cli to fail but it was successful", success);
    }

    @Test
    public void testDmrModelUpdate() throws Exception {
        try {
            boolean success = executeCliScript(new File("target/test-classes/cli/changelog-update.cli"));
            Assert.assertTrue("Expected changelog-update.cli success but it failed", success);
            assertTableModified("dmr_update");
        } finally {
            removeLiquibaseDmrModel("dmr-model-update-test.xml");
        }
    }

    @Test
    public void testDmrModelUpdateContexts() throws Exception {
        try {
            boolean success = executeCliScript(new File("target/test-classes/cli/changelog-update-with-context.cli"));
            Assert.assertTrue("Expected changelog-update-with-context.cli success but it failed", success);
            assertTableModified("dmr_update_with_context", Arrays.asList("firstname", "id", "lastname", "state"));
        } finally {
            removeLiquibaseDmrModel("dmr-model-update-with-context-test.xml");
        }
    }

    @Test
    public void testDmrModelUpdateLabels() throws Exception {
        try {
            boolean success = executeCliScript(new File("target/test-classes/cli/changelog-update-with-label.cli"));
            Assert.assertTrue("Expected changelog-update-with-label.cli success but it failed", success);
            assertTableModified("dmr_update_with_label", Arrays.asList("firstname", "id", "lastname", "state"));
        } finally {
            removeLiquibaseDmrModel("dmr-model-update-with-label-test.xml");
        }
    }

    @Test
    public void testDmrModelUpdateFailOnError() throws Exception {
        try {
            boolean success = executeCliScript(new File("target/test-classes/cli/changelog-update-with-fail-on-error.cli"));
            Assert.assertTrue("Expected changelog-update-with-fail-on-error.cli success but it failed", success);
            assertTableModified("dmr_update_with_fail_on_error");
        } finally {
            removeLiquibaseDmrModel("dmr-model-update-with-fail-on-error-test.xml");
        }
    }

    @Test
    public void testDmrModelUpdateHostExcludes() throws Exception {
        try {
            System.setProperty("host.name", NetUtil.getLocalHostName());
            boolean success = executeCliScript(new File("target/test-classes/cli/changelog-update-with-host-excludes.cli"));
            Assert.assertTrue("Expected changelog-update-with-host-excludes.cli success but it failed", success);
            assertTableModified("dmr_update_with_host_excludes");
        } finally {
            removeLiquibaseDmrModel("dmr-model-update-with-host-excludes-test.xml");
            System.clearProperty("host.name");
        }
    }

    @Test
    public void testDmrModelUpdateHostIncludes() throws Exception {
        try {
            boolean success = executeCliScript(new File("target/test-classes/cli/changelog-update-with-host-includes.cli"));
            Assert.assertTrue("Expected changelog-update-with-host-includes.cli success but it failed", success);
            success = executeCliCommand(String.format("/subsystem=liquibase/databaseChangeLog=dmr-model-update-with-host-includes-test.xml/:write-attribute(name=host-includes,value=%s)", NetUtil.getLocalHostName()));
            Assert.assertTrue("Expected update of host includes success bit it failed", success);
            assertTableModified("dmr_update_with_host_includes");
        } finally {
            removeLiquibaseDmrModel("dmr-model-update-with-host-includes-test.xml");
        }
    }

    @Test
    public void testDmrModelUpdateDatasource() throws Exception {
        try {
            boolean success = executeCliScript(new File("target/test-classes/cli/changelog-update-with-datasource.cli"));
            Assert.assertFalse("Expected changelog-update-with-datasource.cli to fail but it was successful", success);
        } finally {
            removeLiquibaseDmrModel("dmr-model-update-with-datasource-test.xml");
        }
    }
}
