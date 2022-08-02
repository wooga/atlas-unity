/*
 * Copyright 2021 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package wooga.gradle.unity


import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import wooga.gradle.unity.traits.APICompatibilityLevelSpec
import wooga.gradle.unity.traits.UnityAuthenticationSpec
import wooga.gradle.unity.traits.UnityCommandLineSpec
import wooga.gradle.unity.traits.UnityLicenseSpec
import wooga.gradle.unity.traits.UnitySpec
import wooga.gradle.unity.traits.UnityTestSpec

trait UnityPluginExtension implements UnitySpec,
        UnityTestSpec,
        APICompatibilityLevelSpec,
        UnityLicenseSpec,
        UnityAuthenticationSpec {

    private final DirectoryProperty logsDir = objects.directoryProperty()

    /**
     * @return The directory where logs generated by the Unity Editor process are written to
     */
    DirectoryProperty getLogsDir() {
        logsDir
    }

    void setLogsDir(Provider<Directory> value) {
        logsDir.set(value)
    }

    private final DirectoryProperty reportsDir = objects.directoryProperty()

    /**
     * @return The directory where reports generated by Unity Editor tests are written to
     */
    @Internal
    DirectoryProperty getReportsDir() {
        reportsDir
    }

    void setReportsDir(Provider<Directory> value) {
        reportsDir.set(value)
    }

    void setReportsDir(String value) {
        def file = new File(value)
        reportsDir.set(file)
    }

    private final DirectoryProperty assetsDir = objects.directoryProperty()

    /**
     * @return The Assets directory, where all serialized assets by an Unity project are located
     */
    @Internal
    DirectoryProperty getAssetsDir() {
        assetsDir
    }

    void setAssetsDir(Provider<Directory> value) {
        assetsDir.set(value)
    }

    private final DirectoryProperty pluginsDir = objects.directoryProperty()

    /**
     * @return The Plugins directory, where plugins and libraries used by an Unity project are located
     */
    @Internal
    DirectoryProperty getPluginsDir() {
        pluginsDir
    }

    void setPluginsDir(Provider<Directory> value) {
        pluginsDir.set(value)
    }

    private final Property<Boolean> batchModeForEditModeTest = objects.property(Boolean)

    /**
     * @return Whether edit mode tests should be forced to run in batchmode
     */
    Property<Boolean> getBatchModeForEditModeTest() {
        batchModeForEditModeTest
    }

    void setBatchModeForEditModeTest(Provider<Boolean> value) {
        batchModeForEditModeTest.set(value)
    }

    private final Property<Boolean> batchModeForPlayModeTest = objects.property(Boolean)

    /**
     * @return Whether play mode tests should be forced to run in batchmode
     */
    Property<Boolean> getBatchModeForPlayModeTest() {
        batchModeForPlayModeTest
    }

    void setBatchModeForPlayModeTest(Provider<Boolean> value) {
        batchModeForPlayModeTest.set(value)
    }

    private final Property<Boolean> autoReturnLicense = objects.property(Boolean)

    /**
     * @return Whether a task to return the Unity license should be executed after any task of type {@code UnityTask}.
     * This will only happen if {@code autoActivateUnity is also true}.
     */
    Property<Boolean> getAutoReturnLicense() {
        autoReturnLicense
    }

    final Provider<Boolean> shouldReturnLicense = providerFactory.provider({ getAutoActivateUnity().get() && getAutoReturnLicense().get() })

    void setAutoReturnLicense(Provider<Boolean> value) {
        autoReturnLicense.set(value)
    }

    void setAutoReturnLicense(Boolean value) {
        autoReturnLicense.set(value)
    }

    private final Property<Boolean> autoActivateUnity = objects.property(Boolean)

    /**
     * @return Whether a task to activate the Unity license should be executed before any task of type {@code UnityTask}
     */
    Property<Boolean> getAutoActivateUnity() {
        autoActivateUnity
    }

    void setAutoActivateUnity(Boolean value) {
        autoActivateUnity.set(value)
    }

    void setAutoActivateUnity(Provider<Boolean> value) {
        autoActivateUnity.set(value)
    }

    private Property<String> defaultBuildTarget = objects.property(String)

    /**
     * @return If assigned, what the build target will be assigned to by convention
     */
    Property<String> getDefaultBuildTarget() {
        defaultBuildTarget
    }

    void setDefaultBuildTarget(Provider<String> value) {
        defaultBuildTarget.set(value)
    }

    void setDefaultBuildTarget(String value) {
        defaultBuildTarget.set(value)
    }

    private final List<String> testBuildTargets = new ArrayList<String>()

    /**
     * @return The build targets used for the {@link wooga.gradle.unity.tasks.Test} task
     */
    List<String> getTestBuildTargets() {
        testBuildTargets
    }

    void setTestBuildTargets(Iterable<String> targets) {
        testBuildTargets.clear()
        testBuildTargets.addAll(targets)
    }

    private final Property<Boolean> enableTestCodeCoverage = objects.property(Boolean)

    /**
     * @return true if code coverage is enabled for tests, false otherwise.
     */
    Property<Boolean> getEnableTestCodeCoverage() {
        return enableTestCodeCoverage
    }

    void setEnableTestCodeCoverage(Boolean value) {
        enableTestCodeCoverage.set(value)
    }

    void setEnableTestCodeCoverage(Provider<Boolean> value) {
        enableTestCodeCoverage.set(value)
    }

    /**
     * Returns a {@link java.util.Set} of {@link wooga.gradle.unity.models.BuildTarget} objects to construct unity
     * editmode/playmode tasks.
     * <p>
     * The plugin constructs a series of test tasks based on the returned {@link java.util.Set set}.
     * <p>
     * <b>Example Test Task structure with two test build targets:</b>
     * <pre>
     * {@code
     * :check
     * |--- :test
     * +--- :testEditMode
     * |    +--- :testEditModeAndroid
     * |    |--- :testEditModeIos
     * |--- :testPlayMode
     *      +--- :testPlayModeAndroid
     *      |--- :testPlayModeIos
     *}
     *
     * @return the buildtargets to generate test tasks for
     * @default the a set with the {@code defaultBuildTarget}
     */
    Set<String> getTestBuildTargets(Project project) {

        // If no test targets have been directly assigned through the property,
        // check among the project's properties.
        if (getTestBuildTargets().empty) {
            if (project.properties.containsKey("unity.testBuildTargets")) {
                return project.properties.get("unity.testBuildTargets").toString().split(",").collect({
                    it
                })
            } else if (!defaultBuildTarget.isPresent() ) {
                return new HashSet<String>()
            }
        }

        // If test targets were assigned to the property
        Set<String> targets = new HashSet<String>()
        for (String t : getTestBuildTargets()) {
            targets.add(t)
        }

        if (getDefaultBuildTarget().present) {
            targets.add(defaultBuildTarget.get())
        }

        targets
    }

    private final MapProperty<String, String> upmPackages = objects.mapProperty(String, String)

    /**
     * @return The UPM packages to add
     */
    MapProperty<String, String> getUpmPackages() {
        upmPackages
    }

    void setUpmPackages(MapProperty<String, String> upmPackages) {
        upmPackages.set(upmPackages)
    }
}
