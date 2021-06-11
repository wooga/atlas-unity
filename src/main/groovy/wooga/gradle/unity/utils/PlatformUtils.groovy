package wooga.gradle.unity.utils

import groovy.json.StringEscapeUtils

trait PlatformUtilsImpl {

    private static final String _osName = System.getProperty("os.name").toLowerCase()
    private static final String _osArch = System.getProperty("os.arch").toLowerCase()

    /**
     * @return The name of the operating system, as fetched from the environment
     */
    static String osName() {
        _osName
    }

    /**
     * @return The operating system architecture (64/32 bit)
     */
    static String osArchitecture() {
        _osArch
    }

    /**
     * @return True if the current operating system architecture is 64 bit
     */
    static Boolean is64BitArchitecture() {
        _osArch.contains("64")
    }

    /**
     * @return True if the operating system is Windows
     */
    static boolean isWindows() {
        return (_osName.indexOf("win") >= 0)
    }

    /**
     * @return True if the operating system is OSX
     */
    static boolean isMac() {
        return (_osName.indexOf("mac") >= 0)
    }

    /**
     * @return True if the operating system is Linux
     */
    static boolean isLinux() {
        return (_osName.indexOf("linux") >= 0)
    }

    /**
     * @param path A file path
     * @return A file path corrected for the current platform
     */
    static String escapedPath(String path) {
        if (isWindows()) {
            return StringEscapeUtils.escapeJava(path)
        }
        path
    }

    /**
     * Returns the pat to the usr directory in UNIX
     * @return
     */
    static String getUnixUserHomePath() {
        System.getProperty("user.home")
    }
}

class PlatformUtils implements PlatformUtilsImpl {
}
