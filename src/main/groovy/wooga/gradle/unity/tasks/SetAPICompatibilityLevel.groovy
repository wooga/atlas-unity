package wooga.gradle.unity.tasks

import org.gradle.api.internal.ConventionTask
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import wooga.gradle.unity.APICompatibilityLevel
import wooga.gradle.unity.utils.internal.ProjectSettings

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
    Map<String, APICompatibilityLevel> getApiCompatibilityLevel() {
        def value = apiCompatibilityLevel

        if (!value) {
            return null
        }

        if (value instanceof Closure) {
            value = value.call()
        }

        if (!value) {
            return null
        }

        if (value instanceof Map<String, APICompatibilityLevel>){
            return value
        }

        if (value instanceof APICompatibilityLevel) {
            value = value as APICompatibilityLevel
        }
        else if (value instanceof Integer) {
            value = APICompatibilityLevel.valueOfInt((value))
        }
        else {
            try {
                value = value.toString() as APICompatibilityLevel
            }
            catch (Exception e) {
                throw new Exception(parseFailureMessage)
            }
        }

        value = APICompatibilityLevel.toMap(value)
        return value
    }

    void setApiCompatibilityLevel(Object value) {
        this.apiCompatibilityLevel = value
    }

    void apiCompatibilityLevel(Object value) {
        setApiCompatibilityLevel(value)
    }

    Object apiCompatibilityLevel
    Map<String, APICompatibilityLevel> previousAPICompatibilityLevel

    final static String parseFailureMessage = "Failed to parse API compatibility level"

    SetAPICompatibilityLevel() {
        onlyIf(new Spec<SetAPICompatibilityLevel>() {
            @Override
            boolean isSatisfiedBy(SetAPICompatibilityLevel t) {
                t.settingsFile != null
                t.apiCompatibilityLevel != null
            }
        })
        onlyIf(new Spec<SetAPICompatibilityLevel>() {
            @Override
            boolean isSatisfiedBy(SetAPICompatibilityLevel t) {
                def file = getSettingsFile()
                def projectSettings = new ProjectSettings(file)

                def currentAPICompLevel = projectSettings.getAPICompatibilityLevelPerPlatform()
                def newAPICompLevel = getApiCompatibilityLevel()

                return currentAPICompLevel != newAPICompLevel
            }
        })
    }

    @TaskAction
    protected void onExecute() {

        def file = getSettingsFile()
        def projectSettings = new ProjectSettings(file)

        previousAPICompatibilityLevel = projectSettings.getAPICompatibilityLevelPerPlatform()
        if (previousAPICompatibilityLevel == null) {
            logger.warn("No previous API compatibility level was set")
        }

        Map<String, APICompatibilityLevel> apiLevel = getApiCompatibilityLevel()
        logger.info("Setting API compatibility level to ${apiLevel}")
        projectSettings.setAPICompatibilityLevelForSupportedPlatforms(apiLevel)
        projectSettings.write(file)
    }

}
