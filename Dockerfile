FROM jboss/wildfly:16.0.0.Final

COPY ./distro/target/modules/ /opt/jboss/wildfly/modules/system/layers/base/
COPY ./distro/target/configuration/* /opt/jboss/wildfly/standalone/configuration/

CMD [ "/opt/jboss/wildfly/bin/standalone.sh", "-c", "standalone-liquibase.xml", "-b 0.0.0.0", "-bmanagement 0.0.0.0" ]
