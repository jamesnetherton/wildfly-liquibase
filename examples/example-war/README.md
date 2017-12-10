### WildFly Liquibase Example WAR Deployment

Example of executing a Liquibase change log with a WAR deployment. The change log file can be found at `WEB-INF/create-person-table-changelog.xml`.

When the application is deployed, it will trigger 4 change sets to be run. The result is that a database table named 'person' is created within the WildFly ExampleDS in memory H2 database.

The WildFly server log should show the change log being executed:

```
21:49:18,904 ERROR [stderr] (MSC service thread 1-6) INFO 27/11/17 21:49: liquibase: Successfully acquired change log lock
21:49:18,973 ERROR [stderr] (MSC service thread 1-6) INFO 27/11/17 21:49: liquibase: Creating database history table with name: PUBLIC.DATABASECHANGELOG
21:49:18,976 ERROR [stderr] (MSC service thread 1-6) INFO 27/11/17 21:49: liquibase: Reading from PUBLIC.DATABASECHANGELOG
21:49:18,996 ERROR [stderr] (MSC service thread 1-6) INFO 27/11/17 21:49: liquibase: wildfly-liquibase-changelog.xml: wildfly-liquibase-changelog.xml::1::wildfly: Table person created
21:49:18,996 ERROR [stderr] (MSC service thread 1-6) INFO 27/11/17 21:49: liquibase: wildfly-liquibase-changelog.xml: wildfly-liquibase-changelog.xml::1::wildfly: ChangeSet wildfly-liquibase-changelog.xml::1::wildfly ran successfully in 8ms
21:49:19,016 ERROR [stderr] (MSC service thread 1-6) INFO 27/11/17 21:49: liquibase: wildfly-liquibase-changelog.xml: wildfly-liquibase-changelog.xml::2::wildfly: Columns username(varchar(8)) added to person
21:49:19,017 ERROR [stderr] (MSC service thread 1-6) INFO 27/11/17 21:49: liquibase: wildfly-liquibase-changelog.xml: wildfly-liquibase-changelog.xml::2::wildfly: ChangeSet wildfly-liquibase-changelog.xml::2::wildfly ran successfully in 15ms
21:49:19,027 ERROR [stderr] (MSC service thread 1-6) INFO 27/11/17 21:49: liquibase: wildfly-liquibase-changelog.xml: wildfly-liquibase-changelog.xml::3::wildfly: Columns age(int) added to person
21:49:19,027 ERROR [stderr] (MSC service thread 1-6) INFO 27/11/17 21:49: liquibase: wildfly-liquibase-changelog.xml: wildfly-liquibase-changelog.xml::3::wildfly: ChangeSet wildfly-liquibase-changelog.xml::3::wildfly ran successfully in 8ms
21:49:19,031 ERROR [stderr] (MSC service thread 1-6) INFO 27/11/17 21:49: liquibase: Successfully released change log lock
```

Browse to http://localhost:8080/wildfly-liquibase-example-war and you'll see the names of the columns belonging to the 'person' table.

```
21:49:19,196 INFO  [stdout] (ServerService Thread Pool -- 64) ======> age
21:49:19,196 INFO  [stdout] (ServerService Thread Pool -- 64) ======> firstname
21:49:19,196 INFO  [stdout] (ServerService Thread Pool -- 64) ======> id
21:49:19,197 INFO  [stdout] (ServerService Thread Pool -- 64) ======> lastname
21:49:19,197 INFO  [stdout] (ServerService Thread Pool -- 64) ======> state
21:49:19,197 INFO  [stdout] (ServerService Thread Pool -- 64) ======> username
```

#### Building

```
mvn clean package
```

#### Deploying

```
cp target/wildfly-liquibase-example-war.war ${JBOSS_HOME}/standalone/deployments/
```
