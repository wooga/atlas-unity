/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package wooga.gradle.unity.batchMode

class UnityLogErrorReader {

    static String DEFAULT_MESSAGE = "no error"

    static String readErrorMessageFromLog(File logfile) {
        def message = DEFAULT_MESSAGE
        if(!logfile.exists()) {
            return message
        }

        boolean foundErrorMarker = false
        String errorMarker = "Aborting batchmode due to failure:"
        String line
        logfile.withReader { reader ->
            while ((line = reader.readLine())!=null) {
                if(foundErrorMarker) {
                    message = line
                    break
                }
                if(line.startsWith(errorMarker)) {
                    foundErrorMarker = true
                }
            }
        }

        message
    }
}
