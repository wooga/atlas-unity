package wooga.gradle.unity.tasks

import com.wooga.gradle.PlatformUtils
import com.wooga.gradle.test.IntegrationSpec

import java.util.concurrent.TimeUnit

class ClearDanglingUnityFilesIntegrationSpec extends IntegrationSpec {

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
         runTasksSuccessfully("testTask")

        then:
        tempFolder.exists() == !shouldCleanup

        cleanup:
        fakeProcess?.destroy()
        editorInstanceFile.delete()


        where:
        hasOpenProcess | hasEditorInstanceFile | terminateProcess | shouldCleanup | prefix            | suffix
        false          | false                 | false            | true          | "should delete"   | "no running Unity process with the project [0]"
        false          | false                 | true             | true          | "should delete"   | "no running Unity process with the project [1]"
        true           | true                  | true             | true          | "should delete"   | "running process with the project [1]"
        true           | false                 | false            | true          | "should delete"   | "running process with the project [2]"
        true           | true                  | false            | false         | "shouldnt delete" | "running process with the project"
    }

    def createFakeFrozenUnityExecutable() {
        def fakeFrozenUnity = new File(projectDir, "Unity").with {
            it.createNewFile()
            it.executable = true
            return it
        }
        if (PlatformUtils.windows) {
            fakeFrozenUnity <<
                    """@echo off
            echo 'started'
            :loop
            timeout /t 0.010 >nul
            goto loop
            """
        } else {
            fakeFrozenUnity <<
                    """
            #!/bin/bash
            
            # Trap the SIGINT signal (Ctrl+C) and execute a function
            trap 'exit' SIGINT
            
            echo "started"
            # Infinite loop
            while true
            do
                sleep 0.010
            done
            """
        }
        return fakeFrozenUnity
    }
}
