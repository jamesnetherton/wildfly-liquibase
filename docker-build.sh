#!/bin/bash

POM_WF_VERSION=$(grep -oP '(?<=<version.org.wildfly>).*?(?=</version.org.wildfly>)' pom.xml)
DOCKERFILE_WF_VERSION=$(grep FROM Dockerfile | cut -f2 -d:)

if [[ "${POM_WF_VERSION}" != "${DOCKERFILE_WF_VERSION}" ]]; then
  echo "Dockerfile WildFly base image tag '${DOCKERFILE_WF_VERSION}' does not match pom.xml WildFly version '${POM_WF_VERSION}'."
  exit 1  
fi

if [[ "${TRAVIS_JDK_VERSION}" == "openjdk11" ]]; then

  if [[ ! -d ${TRAVIS_BUILD_DIR}/distro/target ]];
  then
    mvn clean package -pl distro -am
  fi

  docker build -t jamesnetherton/wildfly-liquibase .

  if [[ ! -z "${DOCKER_USERNAME}" ]] && [[ ! -z "${DOCKER_PASSWORD}" ]]; then
    echo "${DOCKER_PASSWORD}" | docker login -u "${DOCKER_USERNAME}" --password-stdin
    docker push jamesnetherton/wildfly-liquibase:latest

    if [[ ! -z "${TRAVIS_TAG}" ]]; then
      docker tag jamesnetherton/wildfly-liquibase:latest jamesnetherton/wildfly-liquibase:${TRAVIS_TAG}
      docker push jamesnetherton/wildfly-liquibase:${TRAVIS_TAG}
    fi
    
    docker logout
  fi
fi
