package wooga.gradle.unity.tasks

import org.gradle.api.internal.ConventionTask
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import wooga.gradle.unity.APICompatibilityLevel
import wooga.gradle.unity.utils.GenericUnityAsset

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

        if (!value) {
            return null
        }

        if (value instanceof APICompatibilityLevel) {
            value = value as APICompatibilityLevel
        }
        else if (value instanceof Integer) {
            value = APICompatibilityLevel.valueOfInt((value))
        }
        else{
            try {
                value = value.toString() as APICompatibilityLevel
            }
            catch (Exception e){
                throw new Exception(parseFailureMessage)
            }
        }

        return value
    }

    void setApiCompatibilityLevel(Object value) {
        this.apiCompatibilityLevel = value
    }

    void apiCompatibilityLevel(Object value) {
        setApiCompatibilityLevel(value)
    }

    Object apiCompatibilityLevel

    APICompatibilityLevel previousAPICompatibilityLevel

    final static String parseFailureMessage = "Failed to parse API compatibility level"
    private final static String apiCompatibilityLevelPropertyPattern = /${APICompatibilityLevel.unityProjectSettingsPropertyKey}:.*$/

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
                def config = new GenericUnityAsset(file)
                def previousAPICompatibilityLevel = APICompatibilityLevel.valueOfInt(config[APICompatibilityLevel.unityProjectSettingsPropertyKey] as Integer)
                return previousAPICompatibilityLevel != getApiCompatibilityLevel()
            }
        })
    }

    @TaskAction
    protected void onExecute() {

        def file = getSettingsFile()
        def config = new GenericUnityAsset(file)
        previousAPICompatibilityLevel = APICompatibilityLevel.valueOfInt(config[APICompatibilityLevel.unityProjectSettingsPropertyKey] as Integer)

        APICompatibilityLevel apiLevel = getApiCompatibilityLevel()
        ant.replaceregexp(file: file.absolutePath,
                match: apiCompatibilityLevelPropertyPattern,
                replace: "${APICompatibilityLevel.unityProjectSettingsPropertyKey}: ${apiLevel.value}",
                byline: true)
        logger.info("Setting API compatibility level to ${apiLevel} (${apiLevel.value})")
    }

}
