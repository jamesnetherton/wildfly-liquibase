#!/bin/bash

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
