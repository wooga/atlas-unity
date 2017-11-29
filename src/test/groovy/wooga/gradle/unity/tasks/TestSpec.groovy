package wooga.gradle.unity.tasks

import nebula.test.ProjectSpec
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import spock.lang.IgnoreIf
import spock.lang.Unroll
import spock.util.environment.RestoreSystemProperties

import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission

class TestSpec extends ProjectSpec {
    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables()

    @IgnoreIf({ System.getProperty("os.name").toLowerCase().contains("windows") })
    @RestoreSystemProperties
    @Unroll
    def "retrieves unity version to determine test runner api with #osName amd #version"() {
        given:
        System.setProperty("os.name", osName)

        and: "a mocked path to unity"
        def unityPathPackage = new File(projectDir, "unity/package/package")
        unityPathPackage.mkdirs()
        def unityPath = new File(unityPathPackage, 'unity')

        and: "a fake Info.plist for mac os x"
        File infoPlist = new File(unityPath.parentFile.parentFile, "Info.plist")
        infoPlist << """
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
        <plist version="1.0">
        <dict>
          <key>NSAppTransportSecurity</key>
          <dict>
            <key>NSAllowsArbitraryLoads</key>
            <true/>
          </dict>
          <key>CFBundleURLTypes</key>
          <array>
            <dict>
              <key>CFBundleURLName</key>
              <string>Unity Asset Store</string>
              <key>CFBundleURLSchemes</key>
              <array>
                <string>com.unity3d.kharma</string>
              </array>
            </dict>
          </array>
          <key>CFBundleDevelopmentRegion</key>
          <string>English</string>
          <key>CFBundleDocumentTypes</key>
          <array>
            <dict>
              <key>CFBundleTypeExtensions</key>
              <array>
                <string>unity</string>
                <string>unityPackage</string>
              </array>
              <key>CFBundleTypeIconFile</key>
              <string>UnityDocumentIcon</string>
              <key>CFBundleTypeOSTypes</key>
              <array>
                <string>????</string>
              </array>
            </dict>
          </array>
          <key>CFBundleExecutable</key>
          <string>Unity</string>
          <key>CFBundleGetInfoString</key>
          <string>Unity version 5.6.0p3 (f8dcc233883f). (c) 2017 Unity Technologies ApS. All rights reserved.</string>
          <key>CFBundleIconFile</key>
          <string>UnityAppIcon</string>
          <key>CFBundleIdentifier</key>
          <string>com.unity3d.UnityEditor5.x</string>
          <key>CFBundleInfoDictionaryVersion</key>
          <string>6.0</string>
          <key>CFBundleName</key>
          <string>Unity</string>
          <key>CFBundlePackageType</key>
          <string>APPL</string>
          <key>CFBundleShortVersionString</key>
          <string>Unity version 5.6.0p3</string>
          <key>CFBundleSignature</key>
          <string>UNED</string>
          <key>CFBundleVersion</key>
          <string>${version}p3</string>
          <key>NSMainNibFile</key>
          <string>MainMenuNew</string>
          <key>NSPrincipalClass</key>
          <string>EditorApplicationPrincipalClass</string>
          <key>UnityBuildNumber</key>
          <string>f8dcc233883f</string>
        </dict>
        </plist>
        """.stripIndent()

        and: "a fake wmic command"
        def bin = new File('/usr/local/bin')
        bin.mkdirs()
        File wmic = new File(bin, "wmic")
        wmic.deleteOnExit()
        wmic << """
        #!/usr/bin/env bash
        echo ${version}p3
        """.stripIndent()
        Files.setPosixFilePermissions(wmic.toPath(), [
                                                        PosixFilePermission.OTHERS_EXECUTE,
                                                        PosixFilePermission.OTHERS_READ,
                                                        PosixFilePermission.OTHERS_WRITE,
                                                        PosixFilePermission.OWNER_EXECUTE,
                                                        PosixFilePermission.OWNER_READ,
                                                        PosixFilePermission.OWNER_WRITE,
                                                        PosixFilePermission.GROUP_READ,
                                                        PosixFilePermission.GROUP_WRITE,
                                                        PosixFilePermission.GROUP_EXECUTE,
        ].toSet())
        environmentVariables.set("PATH", "${projectDir}:${System.getenv()["PATH"]}")

        expect:
        Test.retrieveUnityVersion(project, unityPath, "5.5.0").toString() == version
        wmic.delete()

        where:
        osName     | version    | testRunnerSwitch
        "MAC OS X" | "5.4.0"    | ""
        "MAC OS X" | "5.5.0"    | "-runEditorTests"
        "MAC OS X" | "5.6.0"    | "-runTests"
        "MAC OS X" | "2017.1.0" | "-runTests"
        "MAC OS X" | "2017.2.0" | ""
        "WINDOWS"  | "5.4.0"    | ""
        "WINDOWS"  | "5.5.0"    | "-runEditorTests"
        "WINDOWS"  | "5.6.0"    | "-runTests"
        "WINDOWS"  | "2017.1.0" | "-runTests"
        "WINDOWS"  | "2017.2.0" | ""

    }
}
