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

import spock.lang.Specification
import wooga.gradle.unity.utils.internal.FileUtils

class FileUtilsSpec extends Specification{

    def "ensureFile creates file and parent directories don't exist"() {
        given: "file handle to new file"
        File f = new File(File.createTempDir(), "test/touch.txt")
        assert (!f.exists())

        when: "calling ensureFile"
        FileUtils.ensureFile(f)

        then: "file and parents are created"
        f.exists()
    }

    def "ensureFile does nothing when file already exist"() {
        given: "file handle to new file"
        File f = new File(File.createTempDir(), "touch.txt")
        f.createNewFile()

        assert (f.exists())

        when: "calling ensureFile"
        FileUtils.ensureFile(f)

        then: "file and parents are created"
        f.exists()
    }
}
