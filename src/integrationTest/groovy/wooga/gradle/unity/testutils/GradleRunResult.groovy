package wooga.gradle.unity.testutils

import org.gradle.internal.impldep.org.apache.commons.lang.StringUtils

class GradleRunResult {
    private final ArrayList<String> args;
    private final Map<String, String> envs;

    GradleRunResult(String stdOutput) {
        this(null, stdOutput)
    }

    GradleRunResult(String task, String stdOutput) {
        if(task != null) {
            stdOutput = taskLog(task, stdOutput)
        }
        this.args = loadArgs(stdOutput)
        this.envs = loadEnvs(stdOutput)
    }

    ArrayList<String> getArgs() {
        return args
    }

    Map<String, String> getEnvs() {
        return envs
    }

    boolean argValueMatches(String key, Closure matcher) {
        def argIndex = args.indexOf(key)
        def value = args[argIndex+1]
        return matcher(value)
    }

    private static String taskLog(String task, String stdOutput) {
        String taskString = "> Task ${task}"
        int taskBeginIdx = stdOutput.indexOf(taskString) + taskString.length()
        String taskTail = stdOutput.substring(taskBeginIdx)
        int taskEndIdx = taskTail.indexOf("> Task")
        def logs = taskTail.substring(0, taskEndIdx)
        return logs
    }

    private static ArrayList<String> loadArgs(String stdOutput) {
        def argumentsStartToken = "[ARGUMENTS]:"
        def lastExecutionOffset = stdOutput.lastIndexOf(argumentsStartToken)
        if(lastExecutionOffset < 0) {
            System.out.println(stdOutput)
            throw new IllegalArgumentException("couldn't find arguments list in stdout")
        }
        def lastExecTailString = stdOutput.substring(lastExecutionOffset)
        def argsString = substringBetween(lastExecTailString, argumentsStartToken, "Mock Unity Started").
                replace(argumentsStartToken, "")
        def parts = argsString.split(" ").
                findAll {!StringUtils.isEmpty(it) }.collect{ it.trim() }
        return parts
    }

    private static Map<String, String> loadEnvs(String stdOutput) {
        String environmentStartToken = "[ENVIRONMENT]:"
        def argsString = substringBetween(stdOutput, environmentStartToken, "[ARGUMENTS]").
                replace(environmentStartToken, "")
        def parts = argsString.split(System.lineSeparator()).
                findAll {!StringUtils.isEmpty(it) }.collect{ it.trim() }
        return parts.collectEntries {
            return it.split("=", 2)
        }
    }
    private static String substringBetween(String base, String from, String to) {
        def customArgsIndex = base.indexOf(from)
        def tailString = base.substring(customArgsIndex)
        def endIndex = tailString.indexOf(to)
        return tailString.substring(0, endIndex)
    }
}

