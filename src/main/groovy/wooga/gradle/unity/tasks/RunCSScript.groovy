package wooga.gradle.unity.tasks

import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import wooga.gradle.unity.UnityTask

import java.nio.file.Files

class RunCSScript extends UnityTask {

    private final RegularFileProperty sourceCsScript = objects.fileProperty()

    @InputFile
    RegularFileProperty getSourceCsScript() {
        return sourceCsScript
    }

    void setSourceCsScript(Provider<RegularFile> sourceCsScript) {
        this.sourceCsScript.set(sourceCsScript)
    }

    void setSourceCsScript(RegularFile sourceCsScript) {
        this.sourceCsScript.set(sourceCsScript)
    }

    void setSourceCsScript(File sourceCsScript) {
        this.sourceCsScript.set(sourceCsScript)
    }

    private final RegularFileProperty destCsScript = objects.fileProperty()

    @InputFile
    RegularFileProperty getDestCsScript() {
        return destCsScript
    }

    void setDestCsScript(Provider<RegularFile> destCsScript) {
        this.destCsScript.set(destCsScript)
    }

    void setDestCsScript(RegularFile destCsScript) {
        this.destCsScript.set(destCsScript)
    }

    void setDestCsScript(File destCsScript) {
        this.destCsScript.set(destCsScript)
    }

    @Override //this input is mandatory for this task, so overriding the previous annotation.
    @Input
    Property<String> getExecuteMethod() {
        return super.getExecuteMethod()
    }

    RunCSScript() {
//        finalizedBy(project.tasks.register("_${this.name}_cleanup") {
//            onlyIf {
//                destCsScript.present && destCsScript.get().asFile.file
//            }
//            doLast { generatorScript.get().asFile.delete() }
//        })
    }

    @Override
    protected void preExecute() {
        Files.copy(sourceCsScript.get().asFile.toPath(), destCsScript.get().asFile.toPath())
        destCsScript.asFile.get().deleteOnExit()
//        if(!executeMethod.present) {
//            throw new IllegalArgumentException("'executeMethod' property should be present to run ")
//        }
    }
}
