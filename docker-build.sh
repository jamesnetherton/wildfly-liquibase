#!/bin/bash

set -e

POM_WF_VERSION=$(grep -oP '(?<=<version.org.wildfly>).*?(?=</version.org.wildfly>)' pom.xml)
DOCKERFILE_WF_VERSION=$(grep FROM Dockerfile | cut -f2 -d:)

if [[ "${POM_WF_VERSION}" != "${DOCKERFILE_WF_VERSION}" ]]; then
  MAJOR_VERSION_POM=$(echo ${POM_WF_VERSION} | cut -f1 -d'.')
  MAJOR_VERSION_DOCKERFILE=$(echo ${DOCKERFILE_WF_VERSION} | cut -f1 -d'.')

  if [[ "${MAJOR_VERSION_POM}" != "${MAJOR_VERSION_DOCKERFILE}" ]]; then
    echo "Dockerfile WildFly base image tag major version '${MAJOR_VERSION_DOCKERFILE}' does not match pom.xml WildFly major version '${MAJOR_VERSION_POM}'."
    exit 1
  fi
fi

if [[ ! -z "${DOCKER_USERNAME}" ]] && [[ ! -z "${DOCKER_PASSWORD}" ]]; then
  docker build --build-arg WILDFLY_LIQUIBASE_VERSION=${BUILD_TAG} -t jamesnetherton/wildfly-liquibase .

  echo "${DOCKER_PASSWORD}" | docker login -u "${DOCKER_USERNAME}" --password-stdin
  docker push jamesnetherton/wildfly-liquibase:latest

  if [[ ! -z "${BUILD_TAG}" ]]; then
    docker tag jamesnetherton/wildfly-liquibase:latest jamesnetherton/wildfly-liquibase:${BUILD_TAG}
    docker push jamesnetherton/wildfly-liquibase:${BUILD_TAG}
  fi

  docker logout
fi
