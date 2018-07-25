# wildfly-liquibase

[![Build Status](https://travis-ci.org/jamesnetherton/wildfly-liquibase.svg?branch=master)](https://travis-ci.org/jamesnetherton/wildfly-liquibase)
[![License](https://img.shields.io/:license-Apache2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.jamesnetherton/wildfly-liquibase.svg?maxAge=600)](http://search.maven.org/#search%7Cga%7C1%7Cg%3Acom.github.jamesnetherton%20a%3Awildfly-liquibase-distro)

[WildFly](http://wildfly.org) subsystem extension for [Liquibase](http://www.liquibase.org/). Enables you to execute Liquibase change logs for your JavaEE applications without having to configure the `LiquibaseServletListener` or bundle Liquibase with your application.

## Installation

Download one of the release distribution zip files and unpack it inside of your WildFly installation directory.

```
cd $JBOSS_HOME
wget https://github.com/jamesnetherton/wildfly-liquibase/releases/download/0.3.0/wildfly-liquibase-distro-0.3.0.zip
unzip wildfly-liquibase-distro-0.3.0.zip
```

Check the release notes to ensure that the distribution is compatible with your WildFly version.

## Configuration

### Subsystem configuration

For convenience, the distribution provides a `standalone-liquibase.xml` configuration file which you can reference when starting WildFly:

```
$JBOSS_HOME/bin/standalone.sh -c standalone-liquibase.xml
```

Otherwise you can manually configure the Liquibase subsystem in one of the existing configuration files as follows.

1. Add the extension as a child element to the `<extensions>` tag:

```
<extension module="com.github.jamesnetherton.extension.liquibase"/>
```

2. Configure the subsystem by adding it under the `<profile>` element (more on this later):

```
<subsystem xmlns="urn:com.github.jamesnetherton.liquibase:1.0"/>
```

### Change logs

Change logs can be configured in three ways.

#### 1. Change log files within deployments

You can package Liquibase change log files within your deployment. The following file extensions are supported:

* .json
* .sql
* .xml
* .yaml
* .yml

The Liquibase subsystem will search for change log files which match the regex `.*changelog.(json|sql|xml|yaml|yml)$` and will attempt to apply them before the deployment is successfully installed.

In order for the Liquibase subsystem to discover your DataSource, you must add it to the WildFly datasources subsystem configuration. You must then reference the
datasource JNDI binding in your change log file via a [change log parameter](http://www.liquibase.org/documentation/changelog_parameters.html) named `datasource-ref`.

```xml
<databaseChangeLog>

    <property name="datasource-ref" value="java:jboss/datasources/ExampleDS" />

    ...
</databaseChangeLog>
```

For [Liquibase formatted](http://www.liquibase.org/documentation/sql_format.html) SQL change log files, `datasource-ref` must be specified within an SQL comment block. For example:

```sql
--liquibase formatted sql

--datasource-ref java:jboss/datasources/ExampleDS

CREATE TABLE test (
    ...
);
```

#### 2. Standalone XML change log file deployment

You can execute XML change logs without the requirement of a deployment archive wrapper. Simply place a file suffixed with `changelog.xml` into the WildFly deployments directory and the Liquibase subsystem will attempt to execute it.

#### 3. Change log files within Liquibase subsystem configuration

Change logs can be defined as part of the Liquibase subsystem configuration. When WildFly starts, it will attempt to apply change logs before any applications are deployed.

The change log definition body must be wraped within a `CDATA` block in order for it to be parsed correctly. Change log definitions can be defined in JSON, SQL, XML or YAML formats. You may specify multiple `<databaseChangeLog>` elements if you wish.

> When defining a change log as XML, the usual Liquibase namespace declarations are not required as these are automatically added for you.

```xml
<subsystem xmlns="urn:com.github.jamesnetherton.liquibase:1.0"/>
    <databaseChangeLog name="changelog.xml" datasource-ref="java:jboss/datasources/ExampleDS" context-names="test">
        <![CDATA[
            <preConditions>
                <runningAs username="SA"/>
            </preConditions>

            <changeSet id="1" author="wildfly" context="test">
                <createTable tableName="person">
                    <column name="id" type="int" autoIncrement="true">
                        <constraints primaryKey="true" nullable="false"/>
                    </column>
                    <column name="firstname" type="varchar(50)"/>
                    <column name="lastname" type="varchar(50)">
                        <constraints nullable="false"/>
                    </column>
                    <column name="state" type="char(2)"/>
                </createTable>
            </changeSet>
        ]]>
    </databaseChangeLog>
</subsystem>
```

**Change Log Attributes**

|Attribute Name| Required | Description|
---------------|----------|-------------
|context-names | No | A comma separated list of Liquibase contexts to run in
|datasource-ref | Yes | A reference to a DataSource JNDI binding configured in the WildFly datasources susbsystem
|name | Yes | Unique identifier for the change log which is ideally a file name. You should include a file extension to help the Liquibase subsystem determine what type of content it is handling

## Examples

Take a look at the [examples](https://github.com/jamesnetherton/wildfly-liquibase/tree/master/examples) to see some basic use cases.
