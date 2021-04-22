package wooga.gradle.unity.tasks

import wooga.gradle.unity.UnityTask
import wooga.gradle.unity.traits.UnityLicenseSpec

class ReturnLicense extends UnityTask implements UnityLicenseSpec {

    @Override
    protected void setCommandLineOptionDefaults() {
        super.setCommandLineOptionDefaults()
        returnLicense = true
    }
}
