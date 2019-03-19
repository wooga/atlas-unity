package wooga.gradle.unity.utils.internal

import org.apache.maven.artifact.versioning.ArtifactVersion
import org.apache.maven.artifact.versioning.DefaultArtifactVersion

class UnityVersionManager {
    static ArtifactVersion retrieveUnityVersion(File pathToUnity, String defaultVersion) {
        def versionString = net.wooga.uvm.UnityVersionManager.readUnityVersion(pathToUnity)
        if(!versionString ) {
            versionString = defaultVersion
        }

        new DefaultArtifactVersion(versionString.split(/f|p/).first().toString())
    }
}
