--liquibase formatted sql

--datasource #DATASOURCE#

--changeset changeset:#ID# author:wildfly
CREATE TABLE #TABLE_NAME# (
    id INT,
    firstname VARCHAR(50),
    lastname VARCHAR(50),
    state CHAR(2)
);

--changeset changeset:#ID# author:wildfly
ALTER TABLE #TABLE_NAME# ADD COLUMN username VARCHAR(8);
