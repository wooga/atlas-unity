package wooga.gradle.unity.models

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class UnityProjectManifest extends HashMap<String, Object> implements GroovyInterceptable {

    UnityProjectManifest(Map<String, Object> map) {
        this.putAll(map)
    }

    static UnityProjectManifest deserialize(File file) {
        Map data = new JsonSlurper().parse(file)
        new UnityProjectManifest(data)
    }

    static UnityProjectManifest deserialize(String serialization) {
        Map data = new JsonSlurper().parseText(serialization)
        new UnityProjectManifest(data)
    }

    String serialize() {
        JsonOutput.prettyPrint(JsonOutput.toJson(this))
    }

    def propertyMissing(String name, Object value) {
        this[name] = value
    }

    def propertyMissing(String name) {
        this[name]
    }

    // ResolutionStrategy
    private static final String resolutionStrategyKey = "resolutionStrategy"

    String getResolutionStrategy() {
        getOrDefault(resolutionStrategyKey, null)
    }

    void setResolutionStrategy(String value) {
        super.put(resolutionStrategyKey, value)
    }

    void setResolutionStrategy(ResolutionStrategy value) {
        setResolutionStrategy(value.toString())
    }

    // Map<String, ?>
    private static final String dependenciesKey = "dependencies"

    Map getDependencies() {
        if (!containsKey(dependenciesKey)) {
            this[dependenciesKey] = [:]
        }

        (Map)this[dependenciesKey]
    }

    void setDependencies(Map<String, Object> map) {
        this[dependenciesKey] = map
    }

    void addDependencies(Map<String, Object> map) {
        getDependencies().putAll(map)
    }

    void addDependency(String key, Object value) {
        getDependencies()[key] = value
    }
}





