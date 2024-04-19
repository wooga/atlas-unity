/*
 * Copyright 2021 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wooga.gradle.unity.traits

import com.sun.org.apache.xpath.internal.operations.Bool
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

    /**
     *  Instanced configuration of a command line option
     */
    static class UnityCommandLineOptionSetting {
        final UnityCommandLineOption option
        final Property<Boolean> enabled
        final Property<String> arguments

        UnityCommandLineOptionSetting(UnityCommandLineOption option, ObjectFactory objects) {
            this.option = option
            this.enabled = objects.property(Boolean)
            this.arguments = objects.property(String)
        }
    }

    private Map<UnityCommandLineOption, UnityCommandLineOptionSetting> commandLineOptions = UnityCommandLineOption.values().collectEntries(
            { [it, new UnityCommandLineOptionSetting(it, objects)] }
    )

    /**
     * @return All command line option instances
     */
    @Internal
    UnityCommandLineOptionSetting[] getCommandLineOptionInstances() {
        commandLineOptions.collect { it -> it.value }
    }

    /**
     * @return Returns the instanced configuration of a command line option
     */
    UnityCommandLineOptionSetting getCommandLineOption(UnityCommandLineOption option) {
        commandLineOptions[option]
    }

    /**
     * Enables or disables a command line option
     */
    void toggleCommandLineOption(UnityCommandLineOption option, Provider<Boolean> value) {
        commandLineOptions[option].enabled.set(value)
    }

    /**
     * Enables a command line option, so that it will be passed in as an option to the Unity Editor process
     * @param option
     */
    void enableCommandLineOption(UnityCommandLineOption option) {
        commandLineOptions[option].enabled.set(true)
    }

    /**
     * Toggles a command line option, so that it will be passed in as an option to the Unity Editor process if enabled
     * @param option
     */
    void toggleCommandLineOption(UnityCommandLineOption option, Boolean value) {
        toggleCommandLineOption(option, providerFactory.provider({ value }))
    }

    /**
     * Sets a command line option with the given argument (while enabling it)
     */
    void setCommandLineOption(UnityCommandLineOption option, Provider<String> args) {
        if (args) {
            commandLineOptions[option].enabled.set(providerFactory.provider({ true }))
            commandLineOptions[option].arguments.set(args)
        }
    }

    /**
     * Sets a command line option with the given argument (while enabling it)
     */
    void setCommandLineOption(UnityCommandLineOption option, String args) {
        setCommandLineOption(option, providerFactory.provider({ args }))
    }

    /**
     * Sets a convention for a command line option with the given argument (while enabling it)
     */
    void setCommandLineOptionConvention(UnityCommandLineOption option, Provider<String> args) {
        if (args) {
            commandLineOptions[option].enabled.set(providerFactory.provider({ true }))
            commandLineOptions[option].arguments.convention(args)
        }
    }

    /**
     * Sets a convention for a command line option with the given argument (while enabling it)
     */
    void setCommandLineOptionConvention(UnityCommandLineOption option, String args) {
        setCommandLineOptionConvention(option, providerFactory.provider({ args }))
    }

    /**
     * Sets a convention for a command line option with the given argument (while enabling it)
     */
    void setCommandLineOptionEnabledConvention(UnityCommandLineOption option, Provider<Boolean> provider) {
        if (provider) {
            commandLineOptions[option].enabled.convention(provider)
        }
    }

    /**
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

    /**
     * @return Given a command line option, returns its currently set arguments o
     * or those that are provided via a fallback
     */
    String getArgumentsOrDefault(UnityCommandLineOption key) {
        def option = commandLineOptions[key]
        option.arguments.getOrElse(getDefaultUnityCommandLineOptionArguments(key))
    }

    /**
     * @return A list of all currently set command line options as flags,
     * in a format that Unity can parse when invoked from the command line
     */
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

    /**
     * @return The default arguments for a given a command line option,
     * which will be retrieved whenever the arguments for an option have not been set manually
     */
    String getDefaultUnityCommandLineOptionArguments(UnityCommandLineOption option) {
        switch (option) {
            case UnityCommandLineOption.projectPath:
                return projectDirectory.get().asFile.path
            case UnityCommandLineOption.buildTarget:
                return buildTarget.get()
            case UnityCommandLineOption.logFile:
                return resolveLogFilePath()
        }
    }

    // TODO: Refactor this out
    String resolveLogFilePath() {
        null
    }

    // -----------------------------------------------------------------------/
    // General
    // -----------------------------------------------------------------------/
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
    Property<String> getLogFile() {
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
    Property<Boolean> getQuit() {
        getCommandLineOption(UnityCommandLineOption.quit).enabled
    }

    void setQuit(Provider<Boolean> value) {
        toggleCommandLineOption(UnityCommandLineOption.quit, value)
    }

    void setQuit(Boolean value) {
        toggleCommandLineOption(UnityCommandLineOption.quit, value)
    }

    @Internal
    Property<Boolean> getNoGraphics() {
        getCommandLineOption(UnityCommandLineOption.noGraphics).enabled
    }

    void setNoGraphics(Provider<Boolean> value) {
        toggleCommandLineOption(UnityCommandLineOption.noGraphics, value)
    }

    void setNoGraphics(Boolean value) {
        toggleCommandLineOption(UnityCommandLineOption.noGraphics, value)
    }

    @Internal
    Property<String> getCreateProject() {
        getCommandLineOption(UnityCommandLineOption.createProject).arguments
    }

    void setCreateProject(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.createProject, value)
    }

    void setCreateProject(String value) {
        setCommandLineOption(UnityCommandLineOption.createProject, value)
    }

    @Internal
    Property<Boolean> getReturnLicense() {
        getCommandLineOption(UnityCommandLineOption.returnLicense).enabled
    }
    void setReturnLicense(Provider<Boolean> value) {
        toggleCommandLineOption(UnityCommandLineOption.returnLicense, value)
    }

    void setReturnLicense(Boolean value) {
        toggleCommandLineOption(UnityCommandLineOption.returnLicense, value)
    }

    @Internal
    Property<String> getExecuteMethod(){
        getCommandLineOption (UnityCommandLineOption.executeMethod).arguments
    }
    void setExecuteMethod(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.executeMethod, value)
    }

    void setExecuteMethod(String value) {
        setCommandLineOption(UnityCommandLineOption.executeMethod, value)
    }

    @Internal
    Property<String> getExportPackage(){
        getCommandLineOption (UnityCommandLineOption.exportPackage).arguments
    }
    void setExportPackage(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.exportPackage, value)
    }

    void setExportPackage(String value) {
        setCommandLineOption(UnityCommandLineOption.exportPackage, value)
    }

    @Internal
    Property<Boolean> getDisableAssemblyUpdater() {
        getCommandLineOption(UnityCommandLineOption.disableAssemblyUpdater).enabled
    }
    void setDisableAssemblyUpdater(Provider<Boolean> value) {
        toggleCommandLineOption(UnityCommandLineOption.disableAssemblyUpdater, value)
    }

    void setDisableAssemblyUpdater(Boolean value) {
        toggleCommandLineOption(UnityCommandLineOption.disableAssemblyUpdater, value)
    }

    @Internal
    Property<Boolean> getDeepProfiling() {
        getCommandLineOption(UnityCommandLineOption.deepProfiling).enabled
    }
    void setDeepProfiling(Provider<Boolean> value) {
        toggleCommandLineOption(UnityCommandLineOption.deepProfiling, value)
    }

    void setDeepProfiling(Boolean value) {
        toggleCommandLineOption(UnityCommandLineOption.deepProfiling, value)
    }

    @Internal
    Property<Boolean> getEnableCodeCoverage(){
        getCommandLineOption(UnityCommandLineOption.enableCodeCoverage).enabled
    }
    void setEnableCodeCoverage(Provider<Boolean> value) {
        toggleCommandLineOption(UnityCommandLineOption.enableCodeCoverage, value)
    }
    void setEnableCodeCoverage(Boolean value) {
        toggleCommandLineOption(UnityCommandLineOption.enableCodeCoverage, value)
    }

    @Internal
    Property<String> getCoverageResultsPath() {
        getCommandLineOption(UnityCommandLineOption.coverageResultsPath).arguments
    }
    void setCoverageResultsPath(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.coverageResultsPath, value)
    }
    void setCoverageResultsPath(String value) {
        setCommandLineOption(UnityCommandLineOption.coverageResultsPath, value)
    }

    @Internal
    Property<String> getCoverageHistoryPath() {
        getCommandLineOption(UnityCommandLineOption.coverageHistoryPath).arguments
    }
    void setCoverageHistoryPath(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.coverageHistoryPath, value)
    }

    void setCoverageHistoryPath(String value) {
        setCommandLineOption(UnityCommandLineOption.coverageHistoryPath, value)
    }

    @Internal
    Property<String> getCoverageOptions() {
        getCommandLineOption(UnityCommandLineOption.coverageOptions).arguments
    }

    void setCoverageOptions(String value) {
        setCommandLineOption(UnityCommandLineOption.coverageOptions, value)
    }

    void setCoverageOptions(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.coverageOptions, value)
    }

    @Internal
    Property<Boolean> getDebugCodeOptimization(){
        getCommandLineOption(UnityCommandLineOption.debugCodeOptimization).enabled
    }

    void setDebugCodeOptimization(boolean value) {
        toggleCommandLineOption(UnityCommandLineOption.debugCodeOptimization, value)
    }

    void setDebugCodeOptimization(Provider<Boolean> value) {
        toggleCommandLineOption(UnityCommandLineOption.debugCodeOptimization, value)
    }

    @Internal
    Property<String> getUserName() {
        getCommandLineOption(UnityCommandLineOption.userName).arguments
    }
    void setUserName(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.userName, value)
    }
    void setUserName(String value) {
        setCommandLineOption(UnityCommandLineOption.userName, value)
    }

    @Internal
    Property<String> getPassword() {
        getCommandLineOption(UnityCommandLineOption.password).arguments
    }
    void setPassword(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.password, value)
    }
    void setPassword(String value) {
        setCommandLineOption(UnityCommandLineOption.password, value)
    }

    @Internal
    Property<String> getSerial() {
        getCommandLineOption(UnityCommandLineOption.serial).arguments
    }
    void setSerial(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.serial, value)
    }

    void setSerial(String value) {
        setCommandLineOption(UnityCommandLineOption.serial, value)
    }

    @Input
    @Optional
    Property<String> getBuildTarget() {
        getCommandLineOption(UnityCommandLineOption.buildTarget).arguments
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

    // -----------------------------------------------------------------------/
    // Tests
    // -----------------------------------------------------------------------/
    @Internal
    Property<Boolean> getForgetProjectPath(){
        getCommandLineOption(UnityCommandLineOption.forgetProjectPath).enabled
    }

    void setForgetProjectPath(Provider<Boolean> value) {
        toggleCommandLineOption(UnityCommandLineOption.forgetProjectPath, value)
    }

    void setForgetProjectPath(Boolean value) {
        toggleCommandLineOption(UnityCommandLineOption.forgetProjectPath, value)
    }

    @Internal
    Property<Boolean> getRunTests(){
        getCommandLineOption(UnityCommandLineOption.runTests).enabled
    }
    void setRunTests(Provider<Boolean> value) {
        toggleCommandLineOption(UnityCommandLineOption.runTests, value)
    }

    void setRunTests(Boolean value) {
        toggleCommandLineOption(UnityCommandLineOption.runTests, value)
    }

    @Input
    @Optional
    Property<String> getTestCategory() {
        getCommandLineOption(UnityCommandLineOption.testCategory).arguments
    }
    void setTestCategory(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.testCategory, value)
    }

    void setTestCategory(String value) {
        setCommandLineOption(UnityCommandLineOption.testCategory, value)
    }

    void setTestCategory(String... value) {
        setCommandLineOption(UnityCommandLineOption.testCategory, value.join(";"))
    }

    @Input
    @Optional
    Property<String> getTestFilter() {
        getCommandLineOption(UnityCommandLineOption.testFilter).arguments
    }
    void setTestFilter(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.testFilter, value)
    }

    void setTestFilter(String value) {
        setCommandLineOption(UnityCommandLineOption.testFilter, value)
    }

    void setTestFilter(String... value) {
        setCommandLineOption(UnityCommandLineOption.testFilter, value.join(";"))
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

    @Internal
    Property<String> getAssemblyNames() {
        return getCommandLineOption(UnityCommandLineOption.assemblyNames).arguments
    }
    void setAssemblyNames(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.assemblyNames, value)
    }

    void setAssemblyNames(String value) {
        setCommandLineOption(UnityCommandLineOption.assemblyNames, value)
    }

    void setAssemblyNames(String... value) {
        setCommandLineOption(UnityCommandLineOption.assemblyNames, value.join(";"))
    }

    @Internal
    Property<String> getTestResults() {
        return getCommandLineOption(UnityCommandLineOption.testResults).arguments
    }

    void setTestResults(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.testResults, value)
    }

    void setTestResults(String value) {
        setCommandLineOption(UnityCommandLineOption.testResults, value)
    }

    @Internal
    Property<String> getPlayerHeartbeatTimeout() {
        return getCommandLineOption(UnityCommandLineOption.playerHeartbeatTimeout).arguments
    }
    void setPlayerHeartbeatTimeout(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.playerHeartbeatTimeout, value)
    }

    void setPlayerHeartbeatTimeout(String value) {
        setCommandLineOption(UnityCommandLineOption.playerHeartbeatTimeout, value)
    }

    @Internal
    Property<Boolean> getRunSynchronously(){
        getCommandLineOption(UnityCommandLineOption.runSynchronously).enabled
    }
    void setRunSynchronously(Provider<Boolean> value) {
        toggleCommandLineOption(UnityCommandLineOption.runSynchronously, value)
    }

    void setRunSynchronously(Boolean value) {
        toggleCommandLineOption(UnityCommandLineOption.runSynchronously, value)
    }

    // Cache Server
    @Internal
    Property<Boolean> getEnableCacheServer() {
        getCommandLineOption(UnityCommandLineOption.enableCacheServer).enabled
    }
    void setEnableCacheServer(Provider<Boolean> value) {
        toggleCommandLineOption(UnityCommandLineOption.enableCacheServer, value)
    }
    void setEnableCacheServer(Boolean value) {
        toggleCommandLineOption(UnityCommandLineOption.enableCacheServer, value)
    }

    @Internal
    Property<String> getCacheServerEndPoint() {
        return getCommandLineOption(UnityCommandLineOption.cacheServerEndPoint).arguments
    }
    void setCacheServerEndPoint(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.cacheServerEndPoint, value)
    }

    void setCacheServerEndPoint(String value) {
        setCommandLineOption(UnityCommandLineOption.cacheServerEndPoint, value)
    }

    @Internal
    Property<Boolean> getCacheServerNamespacePrefix() {
        return getCommandLineOption(UnityCommandLineOption.cacheServerNamespacePrefix).arguments
    }
    void setCacheServerNamespacePrefix(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.cacheServerNamespacePrefix, value)
    }

    void setCacheServerNamespacePrefix(String value) {
        setCommandLineOption(UnityCommandLineOption.cacheServerNamespacePrefix, value)
    }

    // Special case because this is a boolean option but needs to be set as a string
    @Internal
    Property<String> getCacheServerEnableDownload() {
        return getCommandLineOption(UnityCommandLineOption.cacheServerEnableDownload).arguments
    }

    void setCacheServerEnableDownload(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.cacheServerEnableDownload, value)
    }

    void setCacheServerEnableDownload(String value) {
        setCommandLineOption(UnityCommandLineOption.cacheServerEnableDownload, value)
    }

    void setCacheServerEnableDownload(Boolean value) {
        setCommandLineOption(UnityCommandLineOption.cacheServerEnableDownload, value.toString())
    }

    // Special case because this is a boolean option but needs to be set as a string
    @Internal
    Property<String> getCacheServerEnableUpload() {
        return getCommandLineOption(UnityCommandLineOption.cacheServerEnableUpload).arguments
    }

    void setCacheServerEnableUpload(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.cacheServerEnableUpload, value)
    }

    void setCacheServerEnableUpload(String value) {
        setCommandLineOption(UnityCommandLineOption.cacheServerEnableUpload, value)
    }

    void setCacheServerEnableUpload(Boolean value) {
        setCommandLineOption(UnityCommandLineOption.cacheServerEnableUpload, value.toString())
    }

    @Internal
    Property<String> getCacheServerIPAddress() {
        return getCommandLineOption(UnityCommandLineOption.cacheServerIPAddress).arguments
    }
    void setCacheServerIPAddress(Provider<String> value) {
        setCommandLineOption(UnityCommandLineOption.cacheServerIPAddress, value)
    }

    void setCacheServerIPAddress(String value) {
        setCommandLineOption(UnityCommandLineOption.cacheServerIPAddress, value)
    }
}
