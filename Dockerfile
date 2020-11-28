FROM jamesnetherton/wildfly:21.0.1.Final

ARG WILDFLY_LIQUIBASE_VERSION

ADD ./distro/target/wildfly-liquibase-distro-${WILDFLY_LIQUIBASE_VERSION}.tar.gz /opt/jboss/wildfly/

CMD [ "/opt/jboss/wildfly/bin/standalone.sh", "-c", "standalone-liquibase.xml", "-b 0.0.0.0", "-bmanagement 0.0.0.0" ]
