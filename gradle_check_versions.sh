#!/usr/bin/env bash

versions=("4.0" "4.1" "4.2" "4.3" "4.4" "4.5" "4.6" "4.7" "4.8" "4.9" "4.10")

for i in "${versions[@]}"
do
    echo "test gradle version $i"
    GRADLE_VERSION=$i ./gradlew integrationTest &> /dev/null
    status=$?
    mkdir -p "build/reports/$i"
    mv build/reports/integrationTest "build/reports/$i"
    if [ $status -ne 0 ]; then
        echo "test error $i"
    fi
done
