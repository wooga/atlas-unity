#!/usr/bin/env bash

#
# Copyright 2021 Wooga GmbH
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

versions=("5.1" "5.2" "5.3" "5.4" "5.5" "5.6" "6.0" "6.1" "6.2" "6.3" "6.4" "6.5" "6.6" "6.7" "6.8")

rm -fr build/reports
for i in "${versions[@]}"
do
    echo "test gradle version $i"
    GRADLE_VERSION=$i ./gradlew test &> /dev/null
    status=$?
    mkdir -p "build/reports/$i"
    mv build/reports/test "build/reports/$i"
    if [ $status -ne 0 ]; then
        echo "test error $i"
    fi

    GRADLE_VERSION=$i ./gradlew integrationTest &> /dev/null
    status=$?
    mkdir -p "build/reports/$i"
    mv build/reports/integrationTest "build/reports/$i"
    if [ $status -ne 0 ]; then
        echo "integrationTest error $i"
    fi
done
