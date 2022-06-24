/*
 * Copyright 2018 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package wooga.gradle

class IntegrationSpec extends com.wooga.gradle.test.IntegrationSpec {

    @Override
    Boolean fileExists(String... path) {
        fileExists(path.join(File.separator))
    }

    def file(String... path) {
        file(path.join(File.separator), projectDir)
    }

    def directory(String... path) {
        directory(path.join(File.separator), projectDir)
    }
}





