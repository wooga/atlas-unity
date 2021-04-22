package wooga.gradle.unity.traits

import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.SkipWhenEmpty

trait UnityLicenseSpec extends UnityBaseSpec {

    private DirectoryProperty licenseDirectory = objects.directoryProperty()
    /**
     * The Unity license directory.
     * Defaults to "/Library/Application Support/Unity/" on macOS and "C:\ProgramData\Unity" on windows.
     * @return file to unity license directory
     */
    @SkipWhenEmpty
    @InputDirectory
    DirectoryProperty getLicenseDirectory() {
        licenseDirectory
    }
    void setLicenseDirectory(Provider<Directory> value){
        licenseDirectory.set(value)
    }

}
