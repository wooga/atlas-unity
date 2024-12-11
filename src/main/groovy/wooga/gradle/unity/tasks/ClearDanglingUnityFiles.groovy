package wooga.gradle.unity.tasks;

import com.wooga.gradle.BaseSpec
import groovy.json.JsonException
import groovy.json.JsonSlurper;
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

public class ClearDanglingUnityFiles extends DefaultTask implements BaseSpec {

    @InputDirectory
    public final DirectoryProperty projectDirectory = objects.directoryProperty();

    DirectoryProperty getProjectDirectory() {
        return projectDirectory
    }

    void setProjectDirectory(Provider<Directory> projectDirectory) {
        this.projectDirectory.set(projectDirectory)
    }

    void setProjectDirectory(File projectDirectory) {
        this.projectDirectory.set(projectDirectory)
    }

    public final Property<Boolean> terminateOpenProcess = objects.property(Boolean)

    @Input
    @Optional
    Property<Boolean> getTerminateOpenProcess() {
        return terminateOpenProcess
    }

    void setTerminateOpenProcess(Boolean terminateOpenProcesses) {
        this.terminateOpenProcess.set(terminateOpenProcesses)
    }

    void setTerminateOpenProcess(Provider<Boolean> terminateOpenProcesses) {
        this.terminateOpenProcess.set(terminateOpenProcesses)
    }

    @TaskAction
    def apply() {
        def projectDir = projectDirectory.get().asFile
        def terminateProcess = terminateOpenProcess.getOrElse(false)

        def maybeProjectProcess = findOpenUnityProcessForProject(projectDir)
        maybeProjectProcess.ifPresent {projectProcess ->
            if (terminateProcess) {
                def terminated = destroyProcess(projectProcess)
                if (!terminated) {
                    logger.warn("Failed to terminate Unity process with PID ${projectProcess.pid()}")
                }
            } else {
                logger.info("Found Unity process with PID ${projectProcess.pid()}, but not terminating as terminateOpenProcess=false. " +
                            "Also won't delete Temp folder.")
            }
        }
        if(maybeProjectProcess.empty || terminateProcess) {
            def tempFolder = new File(projectDir, "Temp")
            logger.info("Deleting the ${tempFolder.absolutePath} folder.")
            tempFolder.deleteDir()
        }
    }


    boolean destroyProcess(ProcessHandle process) {
        logger.info("Terminating Unity process with PID ${process.pid()} (SIGTERM).")
        process.destroy()
        logger.info("Waiting up to 5s for the process to terminate gracefully.")
        def terminated = waitForTermination(process, 5, TimeUnit.SECONDS)
        if (!terminated) {
            logger.info("Unity process with PID ${process.pid()} is still alive, trying to forcibly terminate it (SIGKILL)")
            return process.destroyForcibly()
        }
        logger.info("Process with PID ${process.pid()} terminated gracefully.")
        return terminated
    }

    static boolean waitForTermination(ProcessHandle process, long timeout, TimeUnit timeUnit) {
        try {
            process.onExit().get(timeout, timeUnit)
            return true
        } catch (TimeoutException e) {
            return false
        } catch (Exception e) {
            throw e
        }
    }

    java.util.Optional<ProcessHandle> findOpenUnityProcessForProject(File projectDir) {
        def editorInstanceFile = new File(projectDir, "Library/EditorInstance.json")
        if (!editorInstanceFile.exists()) {
            return java.util.Optional.empty()
        }
        try {
            def editorInstance = new JsonSlurper().parse(editorInstanceFile)
            def processId = java.util.Optional.ofNullable(Integer.parseInt(editorInstance["process_id"].toString()))
            return processId.flatMap {ProcessHandle.of(it) }
        } catch (JsonException | NumberFormatException e) {
            logger.warn("invalid json file in Library/EditorInstance.json, counting it as non-existent : $e.message")
            return java.util.Optional.empty()
        }
    }
}
