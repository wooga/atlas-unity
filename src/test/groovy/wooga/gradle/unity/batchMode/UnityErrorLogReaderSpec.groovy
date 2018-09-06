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

package wooga.gradle.unity.batchMode

import spock.lang.Specification
import wooga.gradle.unity.batchMode.internal.UnityLogErrorReader

class UnityErrorLogReaderSpec extends Specification {

    def "reads error message from logfile"() {
        given: "logfile with error message"
        def log = File.createTempFile("log","log")
        log << """
        Reloading assemblies after script compilation.
        Validating Project structure ... 0.005520 seconds.
        
        System memory in use before: 252.4 MB.
        System memory in use after: 252.5 MB.
        
        Unloading 84 unused Assets to reduce memory usage. Loaded Objects now: 817.
        Total: 8.494869 ms (FindLiveObjects: 0.141368 ms CreateObjectMapping: 0.048558 ms MarkObjects: 8.267103 ms  DeleteObjects: 0.036600 ms)
        
        Scripts have compiler errors.
         
        (Filename: /Users/builduser/buildslave/unity/build/Runtime/Utilities/Argv.cpp Line: 171)
        
        
        Aborting batchmode due to failure:
        Scripts have compiler errors.
                 
        (Filename: /Users/builduser/buildslave/unity/build/Runtime/Threads/Posix/PlatformThread.cpp Line: 38)
        """.stripIndent()

        when:"parsing error message"
        def message = UnityLogErrorReader.readErrorMessageFromLog(log)

        then:
        message == "Scripts have compiler errors."
    }

    def "returns default message when error marker is not included"() {
        given: "logfile with no error message"
        def log = File.createTempFile("log","log")
        log << """
        Reloading assemblies after script compilation.
        Validating Project structure ... 0.005520 seconds.
        
        System memory in use before: 252.4 MB.
        System memory in use after: 252.5 MB.
        
        Unloading 84 unused Assets to reduce memory usage. Loaded Objects now: 817.
        Total: 8.494869 ms (FindLiveObjects: 0.141368 ms CreateObjectMapping: 0.048558 ms MarkObjects: 8.267103 ms  DeleteObjects: 0.036600 ms)
        
        Scripts have compiler errors.
         
        (Filename: /Users/builduser/buildslave/unity/build/Runtime/Utilities/Argv.cpp Line: 171)
                         
        (Filename: /Users/builduser/buildslave/unity/build/Runtime/Threads/Posix/PlatformThread.cpp Line: 38)
        """.stripIndent()

        when:"parsing error message"
        def message = UnityLogErrorReader.readErrorMessageFromLog(log)

        then:
        message == UnityLogErrorReader.DEFAULT_MESSAGE
    }

    def "returns default message log file doesn't exist"() {
        given: "path to nonexisting logfile"
        def log = new File("test.log")

        when:"parsing error message"
        def message = UnityLogErrorReader.readErrorMessageFromLog(log)

        then:
        message == UnityLogErrorReader.DEFAULT_MESSAGE
    }
}
