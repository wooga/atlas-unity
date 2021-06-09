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

package wooga.gradle.unity.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.provider.MapProperty
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.TaskAction
import wooga.gradle.unity.models.APICompatibilityLevel
import wooga.gradle.unity.traits.APICompatibilityLevelSpec
import wooga.gradle.unity.utils.ProjectSettingsFile

import javax.inject.Inject

class SetAPICompatibilityLevel extends DefaultTask implements APICompatibilityLevelSpec {

    private MapProperty<String, APICompatibilityLevel> previousAPICompatibilityLevel

    MapProperty<String, APICompatibilityLevel> getPreviousAPICompatibilityLevel(){
        previousAPICompatibilityLevel
    }

    final static String parseFailureMessage = "Failed to parse API compatibility level"

    @Inject
    SetAPICompatibilityLevel() {

        description = "Sets the API compatibility level for a project's platforms"

        previousAPICompatibilityLevel = project.objects.mapProperty(String, APICompatibilityLevel)
        wooga_gradle_unity_traits_APICompatibilityLevelSpec__apiCompatibilityLevel = project.objects.mapProperty(String, APICompatibilityLevel)
        wooga_gradle_unity_traits_APICompatibilityLevelSpec__settingsFile = project.objects.fileProperty()

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
                def file = settingsFile.get().asFile
                if (!file.exists()){
                    logger.warn("No project settings file is present at ${file.path}")
                    return false
                }
                def projectSettings = new ProjectSettingsFile(file)

                Map<String, APICompatibilityLevel> currentAPICompLevel = projectSettings.getAPICompatibilityLevelPerPlatform()
                Map<String, APICompatibilityLevel> targetAPICompLevel = getApiCompatibilityLevel().get()

                if (targetAPICompLevel.size() == 0){
                    return false
                }

                return currentAPICompLevel != targetAPICompLevel
            }
        })
    }

    @TaskAction
    protected void onExecute() {

        def file = settingsFile.get().asFile
        def projectSettings = new ProjectSettingsFile(file)

        previousAPICompatibilityLevel.set(projectSettings.getAPICompatibilityLevelPerPlatform())
        if (previousAPICompatibilityLevel == null) {
            logger.warn("No previous API compatibility level was set")
        }

        def apiLevel = getApiCompatibilityLevel().get()
        logger.info("Setting API compatibility level to ${apiLevel}")
        projectSettings.setAPICompatibilityLevelForSupportedPlatforms(apiLevel)
        projectSettings.write(file)
    }
}
