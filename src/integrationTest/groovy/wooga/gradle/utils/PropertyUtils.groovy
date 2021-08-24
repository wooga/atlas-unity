package wooga.gradle.utils

trait PropertyUtilsImpl {

    static String envNameFromProperty(String extensionName, String property) {
        "${extensionName.toUpperCase()}_${property.replaceAll(/([A-Z])/, "_\$1").toUpperCase()}"
    }

    static String envNameFromProperty(String property) {
        property.replaceAll(/([A-Z.])/, '_$1').replaceAll(/[.]/, '').toUpperCase()
    }
}

class PropertyUtils implements PropertyUtilsImpl {
}

enum PropertyLocation {
    none, script, property, environment

    String reason() {
        switch (this) {
            case script:
                return "value is provided in script"
            case property:
                return "value is provided in properties"
            case environment:
                return "value is set in environment"
            default:
                return "no value was configured"
        }
    }
}
