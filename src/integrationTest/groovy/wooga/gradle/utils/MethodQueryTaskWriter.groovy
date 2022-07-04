package wooga.gradle.utils


import com.wooga.gradle.test.writers.BasePropertyQueryTaskWriter
import nebula.test.functional.ExecutionResult
/**
 * Writes a task for querying the value of a method
 * from a given path with the specified invocation.
 */
class MethodQueryTaskWriter extends BasePropertyQueryTaskWriter {

    final String separator = " : "

    MethodQueryTaskWriter(String path, String taskName = null) {
        super(path, "()", taskName)
    }

    void write(File file) {
        file << """
            task(${taskName}) {
                doLast {
                    def value = ${path}${invocation}
                    println("${path}${separator}" + value)
                }
            }
        """.stripIndent()
    }

    /**
     * @return True if the property's toString() matches the given value
     */
    Boolean matches(ExecutionResult result, Object value) {
        result.standardOutput.contains("${path}${separator}${value}")
    }
}
