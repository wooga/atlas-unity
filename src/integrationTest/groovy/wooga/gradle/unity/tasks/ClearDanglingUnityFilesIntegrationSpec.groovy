package wooga.gradle.unity.tasks

import com.wooga.gradle.PlatformUtils
import com.wooga.gradle.test.IntegrationSpec
import spock.lang.IgnoreIf

import java.util.concurrent.TimeUnit

class ClearDanglingUnityFilesIntegrationSpec extends IntegrationSpec {

    @IgnoreIf({ os.windows })
    def "#prefix unity project Temp folder if there is #suffix"() {
        given:
        def tempFolder = new File(projectDir, "Temp").with {
            it.mkdir()
            new File(it, "UnityLockfile").createNewFile()
            return it
        }
        and:
        Process fakeProcess = null
        if (hasOpenProcess) {
            def fakeUnityExec = createFakeFrozenUnityExecutable()
            fakeProcess = "$fakeUnityExec.absolutePath -projectPath ${projectDir.absolutePath}".execute()
            fakeProcess.waitFor(10, TimeUnit.MILLISECONDS)
        }
        File editorInstanceFile = new File(projectDir, "Library/EditorInstance.json")
        if (hasEditorInstanceFile) {
            editorInstanceFile.parentFile.mkdir()
            editorInstanceFile << """
            {
                "process_id" : ${wrapValueBasedOnType(fakeProcess.pid(), Integer)},
                "version" : "any",
                "app_path" : "any",
                "app_contents_path" : "any"
            }  
            """
        }
        and:
        buildFile << """
        tasks.register("testTask", wooga.gradle.unity.tasks.ClearDanglingUnityFiles) {
            projectDirectory.set(${wrapValueBasedOnType(projectDir, File)})
            terminateOpenProcess.set($terminateProcess)
        }
        """

        when:
        assert tempFolder.exists()
        assert fakeProcess ? fakeProcess.alive : true
        assert editorInstanceFile.exists() == hasEditorInstanceFile
        def result = runTasksSuccessfully("testTask")

        then:
        println result.standardOutput
        tempFolder.exists() == !shouldCleanup

        cleanup:
        fakeProcess?.destroyForcibly()
        editorInstanceFile.delete()


        where:
        hasOpenProcess | hasEditorInstanceFile | terminateProcess | shouldCleanup | prefix            | suffix
        false          | false                 | false            | true          | "should delete"   | "no running Unity process with the project [terminate=false]"
        false          | false                 | true             | true          | "should delete"   | "no running Unity process with the project [terminate=true]"
        true           | true                  | true             | true          | "should delete"   | "running process with the project [terminate=true, EditorInstance.json]"
        true           | false                 | false            | true          | "should delete"   | "running process with the project [terminate=false, no EditorInstance.json]"
        true           | true                  | false            | false         | "shouldnt delete" | "running process with the project [terminate=false, EditorInstance.json]"
    }

    @IgnoreIf({ os.windows })
    def "#prefix terminate Unity process if there is a #suffix"() {
        given:
        def tempFolder = new File(projectDir, "Temp").with {
            it.mkdir()
            new File(it, "UnityLockfile").createNewFile()
            return it
        }
        and:
        def fakeUnityExec = createFakeFrozenUnityExecutable(processIgnoring)
        def fakeProcess = "$fakeUnityExec.absolutePath -projectPath ${projectDir.absolutePath}".execute()
        fakeProcess.waitFor(10, TimeUnit.MILLISECONDS)
        File editorInstanceFile = new File(projectDir, "Library/EditorInstance.json")
        editorInstanceFile.parentFile.mkdir()
        editorInstanceFile << """
        {
            "process_id" : ${wrapValueBasedOnType(fakeProcess.pid(), Integer)},
            "version" : "any",
            "app_path" : "any",
            "app_contents_path" : "any"
        }  
        """
        and:
        buildFile << """
        tasks.register("testTask", wooga.gradle.unity.tasks.ClearDanglingUnityFiles) {
            projectDirectory.set(${wrapValueBasedOnType(projectDir, File)})
            terminateOpenProcess.set($terminateProcess)
        }
        """

        when:
        assert tempFolder.exists()
        assert fakeProcess ? fakeProcess.alive : true
        def result = runTasksSuccessfully("testTask")

        then:
        println result.standardOutput
        terminateProcess == !fakeProcess.alive

        cleanup:
        fakeProcess?.destroyForcibly()
        editorInstanceFile.delete()


        where:
        terminateProcess | processIgnoring       | prefix      | suffix
        false            | []                    | "shouldn't" | "normal running process"
        false            | ["SIGINT", "SIGTERM"] | "shouldn't" | "frozen process"
        true             | []                    | "should"    | "normal running process"
        true             | ["SIGINT", "SIGTERM"] | "should"    | "frozen process"
    }

    def createFakeFrozenUnityExecutable(List<String> signalsToIgnore = []) {
        def fakeFrozenUnity = new File(projectDir, "Unity").with {
            it.createNewFile()
            it.executable = true
            return it
        }
        fakeFrozenUnity <<
                """
        #!/bin/bash
        
        # Trap the SIGINT signal (Ctrl+C) and SIGTERM (kill)
        trap -- '' ${signalsToIgnore.join(" ")}
        
        echo "started"
        # Infinite loop
        while true
        do
            sleep 0.010
        done
        """
        return fakeFrozenUnity
    }
}
