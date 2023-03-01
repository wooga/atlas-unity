package wooga.gradle.unity.utils

import groovy.json.JsonOutput

class PackageManifestBuilder {

    String name = ""

    String getPackageName() {
        name
    }

    String version = ""
    String displayName = ""
    String description = ""

    PackageManifestBuilder() {
    }

    PackageManifestBuilder(String packageName, String version) {
        this.name = packageName
        this.version = version
    }

    String build() {
        JsonOutput.toJson(this)
    }
}
