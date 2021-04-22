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
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import wooga.gradle.unity.models.BuildTarget
import wooga.gradle.unity.traits.APICompatibilityLevelSpec
import wooga.gradle.unity.traits.UnityAuthenticationSpec
import wooga.gradle.unity.traits.UnityLicenseSpec
import wooga.gradle.unity.traits.UnitySpec
import wooga.gradle.unity.traits.UnityTestSpec

trait UnityPluginExtension implements UnitySpec,
        UnityTestSpec,
        APICompatibilityLevelSpec,
        UnityLicenseSpec,
        UnityAuthenticationSpec {

    private final DirectoryProperty logsDir = objects.directoryProperty()

    DirectoryProperty getLogsDir() {
        logsDir
    }

    void setLogsDir(Provider<Directory> value) {
        logsDir.set(value)
    }

    private final DirectoryProperty reportsDir = objects.directoryProperty()

    @Internal
    DirectoryProperty getReportsDir() {
        reportsDir
    }

    void setReportsDir(Provider<Directory> value) {
        reportsDir.set(value)
    }

    private final DirectoryProperty assetsDir = objects.directoryProperty()

    @Internal
    DirectoryProperty getAssetsDir() {
        assetsDir
    }

    void setAssetsDir(Provider<Directory> value) {
        assetsDir.set(value)
    }

    private final DirectoryProperty pluginsDir = objects.directoryProperty()

    @Internal
    DirectoryProperty getPluginsDir() {
        pluginsDir
    }

    void setPluginsDir(Provider<Directory> value) {
        pluginsDir.set(value)
    }

    private final Property<Boolean> batchModeForEditModeTest = objects.property(Boolean)

    Property<Boolean> getBatchModeForEditModeTest() {
        batchModeForEditModeTest
    }

    void setBatchModeForEditModeTest(Provider<Boolean> value) {
        batchModeForEditModeTest.set(value)
    }

    private final Property<Boolean> batchModeForPlayModeTest = objects.property(Boolean)

    Property<Boolean> getBatchModeForPlayModeTest() {
        batchModeForPlayModeTest
    }

    void setBatchModeForPlayModeTest(Provider<Boolean> value) {
        batchModeForPlayModeTest.set(value)
    }

    private final Property<Boolean> autoReturnLicense = objects.property(Boolean)

    Property<Boolean> getAutoReturnLicense() {
        autoReturnLicense
    }

    void setAutoReturnLicense(Provider<Boolean> value) {
        autoReturnLicense.set(value)
    }

    void setAutoReturnLicense(Boolean value) {
        autoReturnLicense.set(value)
    }

    private final Property<Boolean> autoActivateUnity = objects.property(Boolean)

    Property<Boolean> getAutoActivateUnity() {
        autoActivateUnity
    }

    void setAutoActivateUnity(Boolean value) {
        autoActivateUnity.set(value)
    }

    void setAutoActivateUnity(Provider<Boolean> value) {
        autoActivateUnity.set(value)
    }

    private Property<BuildTarget> defaultBuildTarget = objects.property(BuildTarget)

    Property<BuildTarget> getDefaultBuildTarget() {
        defaultBuildTarget
    }

    void setDefaultBuildTarget(Provider<BuildTarget> value) {
        defaultBuildTarget.set(value)
    }

    void setDefaultBuildTarget(String value) {
        defaultBuildTarget.set(value as BuildTarget)
    }

    private final List<Object> testBuildTargets = new ArrayList<Object>()

    List<Object> getTestBuildTargets() {
        testBuildTargets
    }

    void setTestBuildTargets(Iterable<?> targets) {
        testBuildTargets.clear()
        testBuildTargets.addAll(targets)
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
    Set<BuildTarget> getTestBuildTargets(Project project) {

        if (getTestBuildTargets().empty) {
            if (project.properties.containsKey("unity.testBuildTargets")) {
                return EnumSet.copyOf(project.properties.get("unity.testBuildTargets").toString().split(",").collect({
                    it as BuildTarget
                }))
            } else if (defaultBuildTarget.get() == BuildTarget.undefined) {
                return EnumSet.noneOf(BuildTarget)
            }
        }

        List<BuildTarget> targets = new ArrayList<BuildTarget>()
        for (Object t : getTestBuildTargets()) {
            if (t != BuildTarget.undefined) {
                targets.add(t.toString() as BuildTarget)
            }
        }

        if (getDefaultBuildTarget().getOrElse(BuildTarget.undefined) != BuildTarget.undefined) {
            targets.add(defaultBuildTarget.get())
        }

        return EnumSet.copyOf(targets)
    }

}
