#!/usr/bin/env bash

echo "Running tests before commit..."

mvn -s maven-config.xml clean test

RESULT=$?

echo "result: ${RESULT}"

if [[ $RESULT -ne 0 ]]; then
  echo "Tests failed! Commit aborted."
  exit 1
fi
