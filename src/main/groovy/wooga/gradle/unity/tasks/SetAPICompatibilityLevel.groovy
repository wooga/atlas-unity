package wooga.gradle.unity.tasks

import org.gradle.api.internal.ConventionTask
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import wooga.gradle.unity.APICompatibilityLevel

class SetAPICompatibilityLevel extends ConventionTask {

    @InputFile
    File getSettingsFile() {
        project.file(settingsFile)
    }

    void setSettingsFile(Object value) {
        this.settingsFile = value
    }
    private Object settingsFile;

    @Input
    @Optional
    APICompatibilityLevel getApiCompatibilityLevel() {
        def value = apiCompatibilityLevel

        if (!value) {
            return null
        }

        if (value instanceof Closure) {
            value = value.call()
        }

        if (value instanceof APICompatibilityLevel) {
            return value as APICompatibilityLevel
        }

        if (value instanceof Integer) {
            return APICompatibilityLevel.valueOfInt((value))
        }

        return value.toString() as APICompatibilityLevel
    }

    void setApiCompatibilityLevel(Object apiCompatibilityLevel) {
        this.apiCompatibilityLevel = apiCompatibilityLevel
    }
    Object apiCompatibilityLevel

    private final static String apiCompatibilityLevelPropertyPattern = /^\s+${APICompatibilityLevel.unityProjectSettingsPropertyKey}:.*$/

    SetAPICompatibilityLevel() {
        onlyIf(new Spec<SetAPICompatibilityLevel>() {
            @Override
            boolean isSatisfiedBy(SetAPICompatibilityLevel t) {
                t.settingsFile != null
                t.apiCompatibilityLevel != null
            }
        })
    }

    @TaskAction
    protected void onExecute() {
        APICompatibilityLevel apiLevel = getApiCompatibilityLevel()
        ant.replaceregexp(file: getSettingsFile().absolutePath,
                match: apiCompatibilityLevelPropertyPattern,
                replace: "${APICompatibilityLevel.unityProjectSettingsPropertyKey}: ${apiLevel.value}",
                byline: true)
        logger.info("Setting API compatibility level to ${apiLevel} (${apiLevel.value})")
    }

}
