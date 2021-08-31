package wooga.gradle.unity.utils

import java.nio.file.Files
import java.nio.file.Path

class UnityFileTreeFactory {

    public UnityFileTreeFactory() {
    }

    public UnityFileTree fromExecutable(File unityExecutable) {
        def root = getPlatformRootUnityDir(unityExecutable.toPath()).toFile()
        return new UnityFileTree(root, unityExecutable)
    }

    private Path getPlatformRootUnityDir(Path unityExec) {
        Optional<Path> maybeRoot = findUnityRootUsingParent(unityExec.parent)
        if(!maybeRoot.present) {
            maybeRoot = Optional.of(pinpointUnityRoot(unityExec))
        }
        return maybeRoot.get()

    }
    private Optional<Path> findUnityRootUsingParent(Path path) {
        if(path != null && isUnityRoot(path)) {
            return Optional.of(path)
        } else if(path != null && path.root != path) {
            return findUnityRootUsingParent(path.parent)
        }
        return Optional.empty()
    }

    private static boolean isUnityRoot(Path path) {
        def normalizedFolderName = path.fileName.toString().toLowerCase()
        if(PlatformUtils.isWindows()) {
            return normalizedFolderName.startsWith("unity")
        } else if(PlatformUtils.isLinux()) {
            return normalizedFolderName.startsWith("unity")
        } else if(PlatformUtils.isMac()) {
            return normalizedFolderName.endsWith(".app")
        }
        throw new UnsupportedOperationException("OS not supported")
    }

    private static Path pinpointUnityRoot(Path unityExecutable) {
        if(PlatformUtils.isWindows()) {
            return unityExecutable.parent.parent
        } else if(PlatformUtils.isLinux()) {
            return unityExecutable.parent.parent
        } else if(PlatformUtils.isMac()) {
            return unityExecutable.parent.parent.parent
        }
        throw new UnsupportedOperationException("OS not supported")
    }
}

class UnityFileTree {

    private final File unityRoot
    private final File unityExecutable
    private File dotnetExecutable
    private File unityMonoFramework

    static UnityFileTree fromUnityExecutable(File unityExecutable) {
        return new UnityFileTreeFactory().fromExecutable(unityExecutable)
    }

    UnityFileTree(File unityRoot, File unityExecutable) {
        this.unityRoot = unityRoot
        this.unityExecutable = unityExecutable
    }

    File findUnityDotnetExecutable() {
        if(this.unityRoot.exists()) {
            def walkStream = Files.walk(this.unityRoot.toPath())
            return walkStream.filter {currentPath ->
                currentPath.toFile().isFile() && currentPath.fileName.toString().toLowerCase() in ["dotnet", "dotnet.exe"]
            }.findFirst().map{it.toFile()}.orElse(null)
        }
        return null
    }

    File findUnityMonoFramework() {
        if(this.unityRoot.exists()) {
            def walkStream = Files.walk(this.unityRoot.toPath())
            return walkStream.filter {currentPath ->
                currentPath.toFile().isDirectory() && currentPath.fileName.toString() == "MonoBleedingEdge"
            }.findFirst().map{it.toFile()}.orElse(null)
        }
        return null
    }

    File getUnityRoot() {
        return unityRoot
    }

    File getUnityExecutable() {
        return unityExecutable
    }

    File getDotnetExecutable() {
        if(dotnetExecutable == null) {
            this.dotnetExecutable = findUnityDotnetExecutable()
        }
        return this.dotnetExecutable
    }

    File getUnityMonoFramework() {
        if(this.unityMonoFramework == null) {
            this.unityMonoFramework = findUnityMonoFramework()
        }
        return unityMonoFramework
    }
}
