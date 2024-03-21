#!/usr/bin/env bash

set -ex

./gradlew bootJar

# JAVA_OPTS calculation:
# java-buildpack-memory-calculator --loaded-class-count 14000 --thread-count 50 --total-memory 220M --jvm-options "-XX:ReservedCodeCacheSize=32M -XX:MaxMetaspaceSize=80M"
# Output: -XX:MaxDirectMemorySize=10M -Xss1M -Xmx38M
NOW=$(date +"%d-%m-%y_%H-%M")

JAVA_OPTS="-XX:ReservedCodeCacheSize=32M -XX:MaxMetaspaceSize=80M -XX:MaxDirectMemorySize=10M -Xss1M -Xmx500M"

java ${JAVA_OPTS} -jar build/libs/copy-in-repro-0.0.1.jar "$@"
