package wooga.gradle.unity

import org.gradle.authentication.Authentication

trait UnityAuthentication implements Authentication {

    /**
     * Returns the unity account username.
     * <p>
     * The value can be set in multiple ways (gradle properties, environment variable, parameter in code)
     * The precedence order is:
     * <ul>
     *    <li><b>direct parameter in code</b>
     *    <li><b>gradle properties</b>
     *    <li><b>environment variables</b>
     *    <li><b>hardcoded value</b>
     * </ul>
     * @return the username
     * @see UnityPluginConsts#UNITY_USER_PROPERTY
     * @see UnityPluginConsts#UNITY_USER_ENV
     */
    String username

    /**
     * Returns the unity account password.
     * <p>
     * The value can be set in multiple ways (gradle properties, environment variable, parameter in code)
     * The precedence order is:
     * <ul>
     *    <li><b>direct parameter in code</b>
     *    <li><b>gradle properties</b>
     *    <li><b>environment variables</b>
     *    <li><b>hardcoded value</b>
     * </ul>
     * @return the username
     * @see UnityPluginConsts#UNITY_PASSWORD_PROPERTY
     * @see UnityPluginConsts#UNITY_PASSWORD_ENV
     * @return the password
     */
    String password

    /**
     * Returns the Unity serial number.
     * <p>
     * The value can be set in multiple ways (gradle properties, environment variable, parameter in code)
     * The precedence order is:
     * <ul>
     *    <li><b>direct parameter in code</b>
     *    <li><b>gradle properties</b>
     *    <li><b>environment variables</b>
     *    <li><b>hardcoded value</b>
     * </ul>
     * @return the username
     * @see UnityPluginConsts#UNITY_SERIAL_PROPERTY
     * @see UnityPluginConsts#UNITY_SERIAL_ENV
     * @return the serial number
     */
    String serial
}
