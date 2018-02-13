package wooga.gradle.unity

/**
 * Unity Plugin constants
 * <p>
 * This class contains only static constants we use in the plugin.
 */
class UnityPluginConsts {
    /**
     * {@code File} to Unity executable on macOS.
     * @value "/Applications/Unity/Unity.app/Contents/MacOS/Unity"
     */
    static File UNITY_PATH_MAC_OS = new File("/Applications/Unity/Unity.app/Contents/MacOS/Unity")

    /**
     * {@code File} to Unity executable on windows 64bit.
     * @value "C:\Program Files\Unity\Editor\Unity.exe"
     */
    static File UNITY_PATH_WIN = new File("C:\\Program Files\\Unity\\Editor\\Unity.exe")

    /**
     * {@code File} to Unity executable on windows 32bit.
     * @value "C:\Program Files (x86)\Unity\Editor\Unity.exe"
     */
    static File UNITY_PATH_WIN_32 = new File("C:\\Program Files (x86)\\Unity\\Editor\\Unity.exe")

    /**
     * {@code File} to Unity executable on linux.
     * @value "/opt/Unity/Editor/Unity"
     */
    static File UNITY_PATH_LINUX = new File("/opt/Unity/Editor/Unity")

    /**
     * {@code File} to Unity license directory on macOS.
     * @value "/Library/Application Support/Unity/"
     */
    static File UNITY_LICENSE_DIRECTORY_MAC_OS = new File("/Library/Application Support/Unity/")

    /**
     * {@code File} to Unity license directory on windows.
     * @value "C:\ProgramData\Unity"
     */
    static File UNITY_LICENSE_DIRECTORY_WIN = new File("C:\\ProgramData\\Unity")

    /**
     * Gradle property name to set the default value for {@code unityPath}.
     * @value "unity.path"
     * @see UnityPluginConvention#unityPath
     */
    static final String UNITY_PATH_OPTION = "unity.path"

    /**
     * Environment variable name to set the default value for {@code unityPath}.
     * @value "UNITY_PATH"
     * @see UnityPluginConvention#unityPath
     */
    static final String UNITY_PATH_ENV_VAR = "UNITY_PATH"

    /**
     * Gradle property name to set the default value for {@code logCategory}.
     * @value "unity.logCategory"
     * @see UnityPluginConvention#logCategory
     */
    static final String UNITY_LOG_CATEGORY_OPTION = "unity.logCategory"

    /**
     * Environment variable name to set the default value for {@code logCategory}.
     * @value "UNITY_LOG_CATEGORY"
     * @see UnityPluginConvention#logCategory
     */
    static final String UNITY_LOG_CATEGORY_ENV_VAR = "UNITY_LOG_CATEGORY"

    /**
     * Gradle property name to set the default value for {@code redirectStdOut}.
     * @value "unity.redirectStdout"
     * @see UnityPluginConvention#redirectStdOut
     */
    static final String REDIRECT_STDOUT_OPTION = "unity.redirectStdout"

    /**
     * Environment variable name to set the default value for {@code redirectStdOut}.
     * @value "UNITY_REDIRECT_STDOUT"
     * @see UnityPluginConvention#redirectStdOut
     */
    static final String REDIRECT_STDOUT_ENV_VAR = "UNITY_REDIRECT_STDOUT"

    /**
     * Gradle property name to set the default value for {@code authentication.username}.
     * @value "unity.authentication.username"
     * @see UnityAuthentication#username
     */
    static final String UNITY_USER_PROPERTY = "unity.authentication.username"

    /**
     * Environment variable name to set the default value for {@code authentication.username}.
     * @value "UNITY_USR"
     * @see UnityAuthentication#username
     */
    static final String UNITY_USER_ENV = "UNITY_USR"

    /**
     * Gradle property name to set the default value for {@code authentication.password}.
     * @value "unity.authentication.password"
     * @see UnityAuthentication#password
     */
    static final String UNITY_PASSWORD_PROPERTY = "unity.authentication.password"

    /**
     * Environment variable name to set the default value for {@code authentication.password}.
     * @value "UNITY_PWD"
     * @see UnityAuthentication#password
     */
    static final String UNITY_PASSWORD_ENV = "UNITY_PWD"

    /**
     * Gradle property name to set the default value for {@code authentication.serial}.
     * @value "unity.authentication.serial"
     * @see UnityAuthentication#serial
     */
    static final String UNITY_SERIAL_PROPERTY = "unity.serial"

    /**
     * Environment variable name to set the default value for {@code authentication.serial}.
     * @value "UNITY_SERIAL"
     * @see UnityAuthentication#serial
     */
    static final String UNITY_SERIAL_ENV = "UNITY_SERIAL"

}
