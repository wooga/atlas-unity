package wooga.gradle

class FileUtils {
    static void ensureFile(File file) {
        if(!file.exists()) {
            File parent = file.getParentFile()
            if (!parent.exists() && !parent.mkdirs()) {
                throw new IllegalStateException("Couldn't create dir: " + parent)
            }
            file.createNewFile()
        }
    }
}
