package wooga.gradle.unity.traits

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import wooga.gradle.unity.models.BuildTarget
import wooga.gradle.unity.models.TestPlatform
import wooga.gradle.unity.models.UnityCommandLineOption

trait UnityCommandLineSpec extends UnitySpec {

    private Map<UnityCommandLineOption, UnityCommandLineOptionSetting> commandLineOptions = UnityCommandLineOption.values().collectEntries(
            { [it, new UnityCommandLineOptionSetting(it, objects)] }
    )

    UnityCommandLineOptionSetting getCommandLineOption(UnityCommandLineOption option) {
        commandLineOptions[option]
    }

    void toggleCommandLineOption(UnityCommandLineOption option, Provider<Boolean> value) {
        commandLineOptions[option].enabled.set(value)
    }

    void enableCommandLineOption(UnityCommandLineOption option) {
        commandLineOptions[option].enabled.set(true)
    }

    void toggleCommandLineOption(UnityCommandLineOption option, Boolean value) {
        toggleCommandLineOption(option, providerFactory.provider({ value }))
    }

    void setCommandLineOption(UnityCommandLineOption option, Provider<String> args) {
        if (args) {
            commandLineOptions[option].enabled.set(providerFactory.provider({ true }))
            commandLineOptions[option].arguments.set(args)
        }
    }

    void setCommandLineOptionConvention(UnityCommandLineOption option, Provider<String> args) {
        if (args) {
            commandLineOptions[option].enabled.set(providerFactory.provider({ true }))
            commandLineOptions[option].arguments.convention(args)
        }
    }

    void setCommandLineOption(UnityCommandLineOption option, String args) {
        setCommandLineOption(option, providerFactory.provider({ args }))
    }

    void setCommandLineOptionConvention(UnityCommandLineOption option, String args) {
        setCommandLineOptionConvention(option, providerFactory.provider({ args }))
    }

    /**
     *
     * @param key
     * @return True if the command line option is present; if it requires arguments
     * it must also have them set
     */
    Boolean isSet(UnityCommandLineOption key) {
        def option = commandLineOptions[key]
        if (option.enabled.isPresent() && option.enabled.getOrElse(false)) {
            return true
        }
        if (key.hasArguments && option.arguments.isPresent()) {
            return true
        }
        false
    }

    String getArgumentsOrDefault(UnityCommandLineOption key) {
        def option = commandLineOptions[key]
        option.arguments.getOrElse(fetchUnityCommandLineOptionArguments(key))
    }

    @Internal
    List<String> getUnityCommandLineOptions() {
        def result = []
        for (option in commandLineOptions) {

            // Whether the flag has been set
            def enabled = isSet(option.key)
            if (enabled) {

                // The flag (for the shell execution)
                def flag = option.key.flag

                // Whether this option requires arguments,
                def requiresArguments = option.key.hasArguments
                if (requiresArguments) {
                    // If the argument has not yet been provided, try to fetch it
                    def args = getArgumentsOrDefault(option.key)
                    if (args) {
                        result << flag << args
                    }
                } else {
                    result << flag
                }
            }
        }
        result
    }

    String fetchUnityCommandLineOptionArguments(UnityCommandLineOption option) {
        switch (option) {
            case UnityCommandLineOption.projectPath:
                return projectDirectory.get().asFile.path
            case UnityCommandLineOption.buildTarget:
                return buildTarget.get()
            case UnityCommandLineOption.logFile:
                return fetchLogFilePath()
        }
    }

    String fetchLogFilePath() {
        null
    }

    @Internal
    Property<Boolean> getBatchMode() {
        getCommandLineOption(UnityCommandLineOption.batchMode).enabled
    }

    void setBatchMode(Provider<Boolean> value) {
        toggleCommandLineOption(UnityCommandLineOption.batchMode, value)
    }

    void setBatchMode(Boolean value) {
        toggleCommandLineOption(UnityCommandLineOption.batchMode, value)
    }

    void setProjectPath(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.projectPath, value)
    }

    @Internal
    Property<String> getProjectPath() {
        getCommandLineOption(UnityCommandLineOption.projectPath).arguments
    }

    void setProjectPath(String value) {
        setCommandLineOption(UnityCommandLineOption.projectPath, value)
    }

    @Internal
    Property<String> getLogFile(){
        getCommandLineOption(UnityCommandLineOption.logFile).arguments
    }

    void setLogFile(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.logFile, value)
    }

    void setLogFile(String value) {
        setCommandLineOption(UnityCommandLineOption.logFile, value)
    }
    /**
     * Enables the log file option; upon execution the log file path
     * will be queried if its not present
     * @param value
     */
    void toggleLogFile(Boolean value) {
        toggleCommandLineOption(UnityCommandLineOption.logFile, value)
    }

    @Internal
    Property<Boolean> getQuit(){
        getCommandLineOption(UnityCommandLineOption.quit).enabled
    }

    void setQuit(Provider<Boolean> value) {
        toggleCommandLineOption(UnityCommandLineOption.quit, value)
    }

    void setQuit(Boolean value) {
        toggleCommandLineOption(UnityCommandLineOption.quit, value)
    }

    void setNoGraphics(Provider<Boolean> value) {
        toggleCommandLineOption(UnityCommandLineOption.noGraphics, value)
    }

    void setNoGraphics(Boolean value) {
        toggleCommandLineOption(UnityCommandLineOption.noGraphics, value)
    }

    void setCreateProject(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.createProject, value)
    }

    void setCreateProject(String value) {
        setCommandLineOption(UnityCommandLineOption.createProject, value)
    }

    void setReturnLicense(Provider<Boolean> value) {
        toggleCommandLineOption(UnityCommandLineOption.returnLicense, value)
    }

    void setReturnLicense(Boolean value) {
        toggleCommandLineOption(UnityCommandLineOption.returnLicense, value)
    }

    void setRunTests(Provider<Boolean> value) {
        toggleCommandLineOption(UnityCommandLineOption.runTests, value)
    }

    void setRunTests(Boolean value) {
        toggleCommandLineOption(UnityCommandLineOption.runTests, value)
    }

    void setTestResults(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.testResults, value)
    }

    void setTestResults(String value) {
        setCommandLineOption(UnityCommandLineOption.testResults, value)
    }

    @Internal
    Property<String> getTestPlatform() {
        return getCommandLineOption(UnityCommandLineOption.testPlatform).arguments
    }

    void setTestPlatform(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.testPlatform, value)
    }

    void setTestPlatform(String value) {
        setCommandLineOption(UnityCommandLineOption.testPlatform, value)
    }

    void setTestPlatform(TestPlatform value) {
        setCommandLineOption(UnityCommandLineOption.testPlatform, value.toString())
    }

    void setExecuteMethod(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.executeMethod, value)
    }

    void setExecuteMethod(String value) {
        setCommandLineOption(UnityCommandLineOption.executeMethod, value)
    }

    void setExportPackage(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.exportPackage, value)
    }

    void setExportPackage(String value) {
        setCommandLineOption(UnityCommandLineOption.exportPackage, value)
    }

    void setDisableAssemblyUpdater(Provider<Boolean> value) {
        toggleCommandLineOption(UnityCommandLineOption.disableAssemblyUpdater, value)
    }

    void setDisableAssemblyUpdater(Boolean value) {
        toggleCommandLineOption(UnityCommandLineOption.disableAssemblyUpdater, value)
    }

    void setDeepProfiling(Provider<Boolean> value) {
        toggleCommandLineOption(UnityCommandLineOption.deepProfiling, value)
    }

    void setDeepProfiling(Boolean value) {
        toggleCommandLineOption(UnityCommandLineOption.deepProfiling, value)
    }

    void setEnableCodeCoverage(Provider<Boolean> value) {
        toggleCommandLineOption(UnityCommandLineOption.enableCodeCoverage, value)
    }

    void setEnableCodeCoverage(Boolean value) {
        toggleCommandLineOption(UnityCommandLineOption.enableCodeCoverage, value)
    }

    void setUserName(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.userName, value)
    }

    void setUserName(String value) {
        setCommandLineOption(UnityCommandLineOption.userName, value)
    }

    void setPassword(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.password, value)
    }

    void setPassword(String value) {
        setCommandLineOption(UnityCommandLineOption.password, value)
    }

    void setSerial(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.serial, value)
    }

    void setSerial(String value) {
        setCommandLineOption(UnityCommandLineOption.serial, value)
    }

    void setBuildTarget(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.buildTarget, value)
    }

    void setBuildTarget(String value) {
        setCommandLineOption(UnityCommandLineOption.buildTarget, value)
    }

    void setBuildTarget(BuildTarget value) {
        setCommandLineOption(UnityCommandLineOption.buildTarget, value.toString())
    }

    @Input
    @Optional
    Property<String> getBuildTarget() {
        getCommandLineOption(UnityCommandLineOption.buildTarget).arguments
    }

    static class UnityCommandLineOptionSetting {
        final UnityCommandLineOption option
        Property<Boolean> enabled
        Property<String> arguments

        UnityCommandLineOptionSetting(UnityCommandLineOption option, ObjectFactory objects) {
            this.option = option
            this.enabled = objects.property(Boolean)
            this.arguments = objects.property(String)
        }
    }

}

