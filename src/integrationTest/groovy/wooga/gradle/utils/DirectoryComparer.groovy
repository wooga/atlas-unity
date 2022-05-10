package wooga.gradle.utils

// TODO: Move to library
// TODO: Refactor further
/**
 * Compares the contents of two directories
 */
class DirectoryComparer {

    /**
     * The entry of a difference between two files
     */
    class DiffEntry {
        DiffEntry(File left, File right, String reason) {
            this.left = left
            this.right = right
            this.reason = reason
        }
        File left
        File right
        String reason

        File getLeft() {
            left
        }

        File getRight() {
            right
        }

        String getReason() {
            reason
        }

        @Override
        String toString() {
            String result = ""
            result += "Left : ${left != null ? left : ""}"
            result += ", Right : ${right != null ? right : ""}"
            result += " -> ${reason}"
            result
        }
    }

    /**
     * The results of a file comparison between two directories
     */
    class Result {

        int processed
        List<DiffEntry> diff = new ArrayList<DiffEntry>()

        Boolean getValid() {
            diff.size() == 0
        }

        @Override
        String toString() {
            def result = ""
            if (valid) {
                result = "No differences were found"
            }
            else {
                result += "${diff.size()}/${processed} files were found to be different ->"
                diff.each { DiffEntry it ->
                    result += "\n${it.toString()}"
                }
            }
            return super.toString()
        }
    }

    private final File dir1
    private final File dir2
    private List<String> ignoredFilePatterns = new ArrayList<String>()
    private Boolean timestamps = true

    DirectoryComparer(File dir1, File dir2) {
        this.dir1 = dir1
        this.dir2 = dir2
    }

    Result compare() {
        compareDirs(dir1, dir2)
    }

    void ignoreFile(String pattern) {
        ignoredFilePatterns.add(pattern)
    }

    private boolean isIgnored(File file) {
        ignoredFilePatterns.contains(file.name)
    }

    void ignoreTimestamps() {
        timestamps = false
    }

    private Result compareDirs(File dir1, File dir2) {

        Result result = new Result()
        result.diff = []

        // Iterate through the first directory
        dir1.listFiles().each { File left ->

            result.processed++

            // If it's ignored...
            if (isIgnored(left)) {
                return
            }

            def right = new File(dir2, left.name)

            if (!right.exists()) {
                result.diff.add(new DiffEntry(left, null, "Not found on the directory ${dir2}"))
            } else if (left.isDirectory() && !right.isDirectory()) {
                result.diff.add(new DiffEntry(left, right, "It's a directory on ${dir1} but a file on ${dir2}"))
            } else if (!left.isDirectory() && right.isDirectory()) {
                result.diff.add(new DiffEntry(left, right, "It's a file on ${dir1} but a directory on ${dir2}"))
            } else if (left.isDirectory()) {
                def subResult = compareDirs(left, right)
                result.processed += subResult.processed
                result.diff.addAll(subResult.diff)

            } else {
                // Length
                if (left.length() != right.length()) {
                    result.diff.add(new DiffEntry(left, right, "Files have different lengths"))
                }
                // Timestamp
                else if (timestamps && left.lastModified() != right.lastModified()) {
                    result.diff.add(new DiffEntry(left, right, "Files have different timestamps"))
                }
            }
        }

        // Iterate through the second directory
        dir2.listFiles().each { File right ->

            // If it's ignored...
            if (isIgnored(right)) {
                return
            }

            def left = new File(dir1, right.name)

            if (!left.exists()) {
                result.diff.add(new DiffEntry(null, right, "Not found on the directory ${dir1}"))
                result.processed++
            }
        }

        result
    }

}
