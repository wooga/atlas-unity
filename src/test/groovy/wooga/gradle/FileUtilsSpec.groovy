package wooga.gradle

import spock.lang.Specification

class FileUtilsSpec extends Specification{

    def "ensureFile creates file and parent directories don't exist"() {
        given: "file handle to new file"
        File f = new File(File.createTempDir(), "test/touch.txt")
        assert (!f.exists())

        when: "calling ensureFile"
        FileUtils.ensureFile(f)

        then: "file and parents are created"
        f.exists()
    }

    def "ensureFile does nothing when file already exist"() {
        given: "file handle to new file"
        File f = new File(File.createTempDir(), "touch.txt")
        f.createNewFile()

        assert (f.exists())

        when: "calling ensureFile"
        FileUtils.ensureFile(f)

        then: "file and parents are created"
        f.exists()
    }
}
