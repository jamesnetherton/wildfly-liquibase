package com.github.jamesnetherton.extension.liquibase.test.jpa;

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

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import com.github.jamesnetherton.extension.liquibase.test.jpa.init.DatabaseInitializer;
import com.github.jamesnetherton.extension.liquibase.test.jpa.model.Order;
import com.github.jamesnetherton.liquibase.arquillian.LiquibaseTestSupport;
import com.github.jamesnetherton.liquibase.arquillian.TestExtensionUtils;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class LiquibaseHibernateJPAIntegrationTest extends LiquibaseTestSupport {

    @PersistenceContext
    EntityManager em;

    @Resource
    UserTransaction userTransaction;

    @Deployment(order = 1, name = "db-init", testable = false)
    public static Archive<?> dbInit() {
        return ShrinkWrap.create(JavaArchive.class, "liquibase-hibernate-jpa-test-init.jar")
            .addClasses(LiquibaseTestSupport.class, TestExtensionUtils.class, DatabaseInitializer.class)
            .addAsResource("sql/hibernate-jpa-init.sql", "/sql/db-init.sql")
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Deployment(order = 2)
    public static Archive<?> deployment() {
        return ShrinkWrap.create(JavaArchive.class, "liquibase-hibernate-jpa-test.jar")
            .addClass(Order.class)
            .addAsResource("jpa/persistence.xml", "META-INF/persistence.xml")
            .addAsResource("configs/jpa/changelog.xml", "changelog.xml")
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testHibernateValidate() throws Exception {
        Order order = new Order();
        order.setId(1);
        order.setDescription("Test");
        userTransaction.begin();
        em.persist(order);
        userTransaction.commit();
    }
}
