package wooga.gradle.unity.utils

import spock.lang.Requires
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.Paths

class UnityFileTreeTest extends Specification {

    @Unroll
    @Requires({os.isWindows()})
    def "creates unity file tree from given windows unity installation named #rootName"() {
        given:"a unity file tree"
        def tree = fakeWindowsUnityFileTree(rootName)
        when:
        def generatedTree = UnityFileTree.fromUnityExecutable(tree.unityExecutable)
        then:
        generatedTree.unityRoot == tree.unityRoot
        generatedTree.unityExecutable == tree.unityExecutable
        generatedTree.dotnetExecutable == tree.dotnetExecutable
        generatedTree.unityMonoFramework == tree.unityMonoFramework
        cleanup:
        deleteFolder(tree.unityRoot)
        where:
        rootName << ["Unity_win", "win_unity"]
    }


    @Unroll
    @Requires({os.isMacOs()})
    def "creates unity file tree from given macOS unity installation named #rootName"() {
        given:"a unity file tree"
        def tree = fakeMacOSUnityFileTree(rootName)
        when:
        def generatedTree = UnityFileTree.fromUnityExecutable(tree.unityExecutable)
        then:
        generatedTree.unityRoot == tree.unityRoot
        generatedTree.unityExecutable == tree.unityExecutable
        generatedTree.dotnetExecutable == tree.dotnetExecutable
        generatedTree.unityMonoFramework == tree.unityMonoFramework
        cleanup:
        deleteFolder(tree.unityRoot)
        where:
        rootName << ["Unity.app", "unity_mac"]
    }

    @Unroll
    @Requires({os.isLinux()})
    def "creates unity file tree from given linux unity installation named #rootName"() {
        given:"a unity file tree"
        def tree = fakeLinuxUnityFileTree(rootName)
        when:
        def generatedTree = UnityFileTree.fromUnityExecutable(tree.unityExecutable)
        then:
        generatedTree.unityRoot == tree.unityRoot
        generatedTree.unityExecutable == tree.unityExecutable
        generatedTree.dotnetExecutable == tree.dotnetExecutable
        generatedTree.unityMonoFramework == tree.unityMonoFramework
        cleanup:
        deleteFolder(tree.unityRoot)
        where:
        rootName << ["linux_unity", "unity_linux"]
    }

    def fakeWindowsUnityFileTree(String rootname) {
        def root = Paths.get(rootname)
        def executable = Paths.get(root.fileName.toString(), "Editor/Unity.exe")
        def monoDir = Paths.get(root.fileName.toString(), "Editor/Data/MonoBleedingEdge")
        def dotnetExec = Paths.get(root.fileName.toString(), "Editor/Data/NetCore/Sdk-2.2.107/dotnet.exe")
        Files.createDirectories(root)
        Files.createDirectories(monoDir)
        Files.createDirectories(executable.parent)
        Files.createDirectories(dotnetExec.parent)
        executable.toFile().createNewFile()
        dotnetExec.toFile().createNewFile()
        return [unityRoot: root.toFile(),
                unityExecutable: executable.toFile(),
                dotnetExecutable: dotnetExec.toFile(),
                unityMonoFramework: monoDir.toFile()]
    }

    def fakeMacOSUnityFileTree(String rootName) {
        def root = Paths.get(rootName)
        def monoDir = Paths.get(root.fileName.toString(), "Contents/MonoBleedingEdge")
        def executable = Paths.get(root.fileName.toString(), "Contents/MacOS/unity")
        def dotnetExec = Paths.get(root.fileName.toString(), "Contents/NetCore/Sdk-2.2.107/dotnet")
        Files.createDirectories(root)
        Files.createDirectories(monoDir)
        Files.createDirectories(executable.parent)
        Files.createDirectories(dotnetExec.parent)
        executable.toFile().createNewFile()
        dotnetExec.toFile().createNewFile()
        return [unityRoot: root.toFile(),
                unityExecutable: executable.toFile(),
                dotnetExecutable: dotnetExec.toFile(),
                unityMonoFramework: monoDir.toFile()]
    }

    def fakeLinuxUnityFileTree(String rootName) {
        def root = Paths.get(rootName)
        def monoDir = Paths.get(root.fileName.toString(), "Contents/MonoBleedingEdge")
        def executable = Paths.get(root.fileName.toString(), "Editor/Unity")
        def dotnetExec = Paths.get(root.fileName.toString(), "Editor/Data/NetCore/Sdk-2.2.107/dotnet")
        Files.createDirectories(root)
        Files.createDirectories(monoDir)
        Files.createDirectories(executable.parent)
        Files.createDirectories(dotnetExec.parent)
        executable.toFile().createNewFile()
        dotnetExec.toFile().createNewFile()
        return [unityRoot: root.toFile(),
                unityExecutable: executable.toFile(),
                dotnetExecutable: dotnetExec.toFile(),
                unityMonoFramework: monoDir.toFile()]
    }

    def deleteFolder(File folder) {
        Files.walk(folder.toPath()).
                sorted(Comparator.reverseOrder()).map{it.toFile()}.forEach{it.delete()};
    }

}
