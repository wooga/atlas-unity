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

package wooga.gradle.unity.utils

import spock.lang.Specification
import spock.lang.Unroll

class UnityErrorLogReaderTest extends Specification {

    UnityLogErrorReader reader

    def setup(){
        reader = new UnityLogErrorReader()
    }

    def "reads error message from logfile"() {
        given: "logfile with error message"
        def log = File.createTempFile("log", "log")
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

        when: "parsing error message"
        def message = reader.parse(log)

        then:
        message.type == UnityLogErrorType.ScriptCompilerError
    }

    def "returns default message when error marker is not included"() {
        given: "logfile with no error message"
        def log = File.createTempFile("log", "log")
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

        when: "parsing error message"
        def message = reader.parse(log)

        then:
        message.type == UnityLogErrorType.None
    }

    def "returns default message log file doesn't exist"() {
        given: "path to nonexisting logfile"
        def log = new File("test.log")

        when: "parsing error message"
        def message = reader.parse(log)

        then:
        message.type == UnityLogErrorType.None
    }

    def "finds script compiler errors"() {
        given: "logfile with compiler errors"
        def log = File.createTempFile("log", "log")
        log << """
-----CompilerOutput:-stdout--exitcode: 1--compilationhadfailure: True--outfile: Temp/Assembly-CSharp-Editor.dll

Microsoft (R) Visual C# Compiler version 2.9.1.65535 (9d34608e)

Copyright (C) Microsoft Corporation. All rights reserved.

Assets/Wooga/UnifiedBuildSystem/Tests/Editor/BuildEngine/BuildStepTest.cs(9,7): warning CS0105: The using directive for 'Wooga.UnifiedBuildSystem.Editor' appeared previously in this namespace
Assets/Wooga/UnifiedBuildSystem/Tests/Editor/BuildEngine/GroupedBuildStepTest.cs(9,39): error CS0234: The type or namespace name 'Tests' does not exist in the namespace 'Wooga.UnifiedBuildSystem.Editor' (are you missing an assembly reference?)
Assets/Wooga/UnifiedBuildSystem/Samples/Editor/BuildExamples.cs(13,45): warning CS0618: 'BuildStep.RunAfter' is obsolete: 'This ordering is planned to be deprecated. '
Assets/Wooga/UnifiedBuildSystem/Samples/Editor/BuildExamples.cs(16,45): warning CS0618: 'BuildStep.RunBefore' is obsolete: 'This ordering is planned to be deprecated. '
Assets/Wooga/UnifiedBuildSystem/Editor/AppConfig/AppConfig.ISerializationCallbackReceiver.cs(7,21): warning CS0114: 'AppConfig.OnBeforeSerialize()' hides inherited member 'BuildConfigScriptable.OnBeforeSerialize()'. To make the current member override that implementation, add the override keyword. Otherwise add the new keyword.
Assets/Wooga/UnifiedBuildSystem/Editor/AppConfig/AppConfig.ISerializationCallbackReceiver.cs(23,21): warning CS0114: 'AppConfig.OnAfterDeserialize()' hides inherited member 'BuildConfigScriptable.OnAfterDeserialize()'. To make the current member override that implementation, add the override keyword. Otherwise add the new keyword.
Assets/Wooga/UnifiedBuildSystem/Editor/Build/Build.ExportProject.cs(9,41): warning CS0618: 'BuildStep.RunBefore' is obsolete: 'This ordering is planned to be deprecated. '
Assets/Wooga/UnifiedBuildSystem/Editor/Build/Build.PostBuildAndroid.cs(8,46): warning CS0618: 'BuildStep.RunAfter' is obsolete: 'This ordering is planned to be deprecated. '
Assets/Wooga/UnifiedBuildSystem/Editor/Build/Build.PostBuildAndroid.cs(8,83): warning CS0618: 'BuildStep.RunBefore' is obsolete: 'This ordering is planned to be deprecated. '

-----CompilerOutput:-stderr----------

-----EndCompilerOutput---------------

- Finished script compilation in 7.715436 seconds

Assets/Wooga/UnifiedBuildSystem/Tests/Editor/BuildEngine/BuildStepTest.cs(9,7): warning CS0105: The using directive for 'Wooga.UnifiedBuildSystem.Editor' appeared previously in this namespace
Assets/Wooga/UnifiedBuildSystem/Tests/Editor/BuildEngine/GroupedBuildStepTest.cs(9,39): error CS0234: The type or namespace name 'Tests' does not exist in the namespace 'Wooga.UnifiedBuildSystem.Editor' (are you missing an assembly reference?)
Assets/Wooga/UnifiedBuildSystem/Samples/Editor/BuildExamples.cs(13,45): warning CS0618: 'BuildStep.RunAfter' is obsolete: 'This ordering is planned to be deprecated. '
Assets/Wooga/UnifiedBuildSystem/Samples/Editor/BuildExamples.cs(16,45): warning CS0618: 'BuildStep.RunBefore' is obsolete: 'This ordering is planned to be deprecated. '
Assets/Wooga/UnifiedBuildSystem/Editor/AppConfig/AppConfig.ISerializationCallbackReceiver.cs(7,21): warning CS0114: 'AppConfig.OnBeforeSerialize()' hides inherited member 'BuildConfigScriptable.OnBeforeSerialize()'. To make the current member override that implementation, add the override keyword. Otherwise add the new keyword.
Assets/Wooga/UnifiedBuildSystem/Editor/AppConfig/AppConfig.ISerializationCallbackReceiver.cs(23,21): warning CS0114: 'AppConfig.OnAfterDeserialize()' hides inherited member 'BuildConfigScriptable.OnAfterDeserialize()'. To make the current member override that implementation, add the override keyword. Otherwise add the new keyword.
Assets/Wooga/UnifiedBuildSystem/Editor/Build/Build.ExportProject.cs(9,41): warning CS0618: 'BuildStep.RunBefore' is obsolete: 'This ordering is planned to be deprecated. '
Assets/Wooga/UnifiedBuildSystem/Editor/Build/Build.PostBuildAndroid.cs(8,46): warning CS0618: 'BuildStep.RunAfter' is obsolete: 'This ordering is planned to be deprecated. '
Assets/Wooga/UnifiedBuildSystem/Editor/Build/Build.PostBuildAndroid.cs(8,83): warning CS0618: 'BuildStep.RunBefore' is obsolete: 'This ordering is planned to be deprecated. '

Unloading 1218 Unused Serialized files (Serialized files now loaded: 0)
System memory in use before: 321.0 MB.
System memory in use after: 321.2 MB.

...

Unloading 1798 Unused Serialized files (Serialized files now loaded: 0)
System memory in use before: 348.5 MB.
System memory in use after: 347.6 MB.

Unloading 1814 unused Assets to reduce memory usage. Loaded Objects now: 1668.
Total: 7.968787 ms (FindLiveObjects: 0.574456 ms CreateObjectMapping: 0.243863 ms MarkObjects: 4.823242 ms  DeleteObjects: 2.326409 ms)

Disconnect from CacheServer
Refreshing native plugins compatible for Editor in 4.99 ms, found 0 plugins.
Preloading 0 native plugins for Editor in 0.00 ms.
Warming cache for 1797 main assets: 0.003021 seconds elapsed

Initializing Unity extensions:

 '/Users/jenkins_agent/Applications/Unity/Hub/Editor/2019.4.19f1/Unity.app/Contents/UnityExtensions/Unity/UnityVR/Editor/UnityEditor.VR.dll'  GUID: 4ba2329b63d54f0187bcaa12486b1b0f

Unloading 53 Unused Serialized files (Serialized files now loaded: 0)

System memory in use before: 309.1 MB.
System memory in use after: 309.1 MB.
Unloading 56 unused Assets to reduce memory usage. Loaded Objects now: 1678.
Total: 5.620424 ms (FindLiveObjects: 0.363267 ms CreateObjectMapping: 0.103867 ms MarkObjects: 5.024895 ms  DeleteObjects: 0.127224 ms)


Scripts have compiler errors. 
(Filename: ./Runtime/Utilities/Argv.cpp Line: 376)

Aborting batchmode due to failure:
Scripts have compiler errors.

[Package Manager] Server::Kill -- Server was shutdown

(lldb) command source -s 0 '/tmp/mono-gdb-commands.Fr24P1'


> Task :Wooga.UnifiedBuildSystem:exportUnityPackage FAILED
> Task :Wooga.UnifiedBuildSystem:returnUnityLicense SKIPPED
> Task :Wooga.UnifiedBuildSystem:unsetAPICompatibilityLevel SKIPPED

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':Wooga.UnifiedBuildSystem:exportUnityPackage'.

> Unity batchmode finished with non-zero exit value 1 and error 'Scripts have compiler errors.'

* Try:

Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.

* Get more help at https://help.gradle.org

Deprecated Gradle features were used in this build, making it incompatible with Gradle 5.0.
Use '--warning-mode all' to show the individual deprecation warnings.
See https://docs.gradle.org/4.10.2/userguide/command_line_interface.html#sec:command_line_warnings

BUILD FAILED in 2m 24s
10 actionable tasks: 9 executed, 1 up-to-date

"""
        when: "parsing error message"
        def message = reader.parse(log)

        then:
        message.type == UnityLogErrorType.ScriptCompilerError
        message.compilerOutput.size() == 9
        message.compilerOutput[0].properties == new CSharpFileCompilationResult(
                "Assets/Wooga/UnifiedBuildSystem/Tests/Editor/BuildEngine/BuildStepTest.cs(9,7): warning CS0105: The using directive for 'Wooga.UnifiedBuildSystem.Editor' appeared previously in this namespace",
                "Assets/Wooga/UnifiedBuildSystem/Tests/Editor/BuildEngine/BuildStepTest.cs",
                9,
                7,
                "warning",
                "CS0105",
                "The using directive for 'Wooga.UnifiedBuildSystem.Editor' appeared previously in this namespace"
        ).properties

    }

    @Unroll("parses #testCase[0]")
    def "parses csharp file compilation result"() {
        given:
        def input = testCase[0]
        def expected = testCase[1]

        when:
        def result = CSharpFileCompilationResult.Parse(input)

        then:
        result != null
        result.properties == expected.properties

        where:
        testCase << [
                [
                        "A/B.cs(9,7): warning CS0105: WRONG!",
                        new CSharpFileCompilationResult(
                                "A/B.cs(9,7): warning CS0105: WRONG!",
                                "A/B.cs",
                                9,
                                7,
                                "warning",
                                "CS0105",
                                "WRONG!")
                ],
                [
                        "Assets/Wooga/UnifiedBuildSystem/Tests/Editor/BuildEngine/BuildStepTest.cs(9,7): warning CS0105: The using directive for 'Wooga.UnifiedBuildSystem.Editor' appeared previously in this namespace",
                        new CSharpFileCompilationResult(
                                "Assets/Wooga/UnifiedBuildSystem/Tests/Editor/BuildEngine/BuildStepTest.cs(9,7): warning CS0105: The using directive for 'Wooga.UnifiedBuildSystem.Editor' appeared previously in this namespace",
                                "Assets/Wooga/UnifiedBuildSystem/Tests/Editor/BuildEngine/BuildStepTest.cs",
                                9,
                                7,
                                "warning",
                                "CS0105",
                                "The using directive for 'Wooga.UnifiedBuildSystem.Editor' appeared previously in this namespace")
                ],
                [
                        "Assets/Wooga/UnifiedBuildSystem/Tests/Editor/BuildEngine/GroupedBuildStepTest.cs(9,39): error CS0234: The type or namespace name 'Tests' does not exist in the namespace 'Wooga.UnifiedBuildSystem.Editor' (are you missing an assembly reference?)",
                        new CSharpFileCompilationResult(
                                "Assets/Wooga/UnifiedBuildSystem/Tests/Editor/BuildEngine/GroupedBuildStepTest.cs(9,39): error CS0234: The type or namespace name 'Tests' does not exist in the namespace 'Wooga.UnifiedBuildSystem.Editor' (are you missing an assembly reference?)",
                                "Assets/Wooga/UnifiedBuildSystem/Tests/Editor/BuildEngine/GroupedBuildStepTest.cs",
                                9,
                                39,
                                "error",
                                "CS0234",
                                "The type or namespace name 'Tests' does not exist in the namespace 'Wooga.UnifiedBuildSystem.Editor' (are you missing an assembly reference?)")
                ],
        ]

    }

}
