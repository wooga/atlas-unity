package wooga.gradle.unity.utils

class PackageManifestBuilder {

    String packageName = ""
    String version = ""
    String displayName = ""
    String description = ""

    PackageManifestBuilder() {
    }

    PackageManifestBuilder(String packageName, String version) {
        this.packageName = packageName
        this.version = version
    }

    String build() {
        """{
    "name" : "${packageName}",
    "version" : "${version}",
    "displayName" : "${displayName}",
    "description" : "${description}"
}
"""
    }
}
