package wooga.gradle.unity.tasks

import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import wooga.gradle.unity.UnityTask

import java.nio.file.Files

/**
 * Copies a script from {@code sourceScript} into a temporary {@code destinationScript} then executes Unity with {@code executeMethod}
 */
class ExecuteCsharpScript extends UnityTask {

    private final RegularFileProperty sourceScript = objects.fileProperty()

    @InputFile
    RegularFileProperty getSourceScript() {
        return sourceScript
    }

    void setSourceScript(Provider<RegularFile> sourceCsScript) {
        this.sourceScript.set(sourceCsScript)
    }

    void setSourceScript(RegularFile sourceCsScript) {
        this.sourceScript.set(sourceCsScript)
    }

    void setSourceScript(File sourceCsScript) {
        this.sourceScript.set(sourceCsScript)
    }

    private final RegularFileProperty destinationScript = objects.fileProperty()

    @InputFile
    RegularFileProperty getDestinationScript() {
        return destinationScript
    }

    void setDestinationScript(Provider<RegularFile> destCsScript) {
        this.destinationScript.set(destCsScript)
    }

    void setDestinationScript(RegularFile destCsScript) {
        this.destinationScript.set(destCsScript)
    }

    void setDestinationScript(File destCsScript) {
        this.destinationScript.set(destCsScript)
    }

    @Override //this input is mandatory for this task, so overriding the previous annotation.
    @Input
    Property<String> getExecuteMethod() {
        return super.getExecuteMethod()
    }

    ExecuteCsharpScript() {
        finalizedBy(project.tasks.register("_${this.name}_cleanup") {
            onlyIf {
                destinationScript.present && destinationScript.get().asFile.file
            }
            doLast {
                def baseFile = destinationScript.get().asFile
                def metafile = new File(baseFile.absolutePath + ".meta")
                baseFile.delete()
                metafile.delete()
            }
        })
    }

    @Override
    protected void preExecute() {
        Files.copy(sourceScript.get().asFile.toPath(), destinationScript.get().asFile.toPath())
        destinationScript.asFile.get().deleteOnExit()
    }
}
