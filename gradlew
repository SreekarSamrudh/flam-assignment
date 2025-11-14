#!/bin/bash

GRADLE_USER_HOME="$HOME/.gradle"
GRADLE_OPTS="-Xmx2048m"

cd "$(dirname "$0")"

if [ ! -f gradle/wrapper/gradle-wrapper.jar ]; then
    echo "Downloading Gradle wrapper..."
    curl -o gradle/wrapper/gradle-wrapper.jar https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar
fi

java -jar gradle/wrapper/gradle-wrapper.jar "$@"
