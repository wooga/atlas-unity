package wooga.gradle.unity

import org.gradle.api.Action

trait UnityPluginAuthentication<T extends UnityPluginAuthentication> {

    /**
     * Returns a {@code true} if the Unity license should be returned after all Unity tasks have been executed.
     * @return {@code true} if the license should be auto returned
     * @default true
     * @see wooga.gradle.unity.tasks.ReturnLicense
     */
    Boolean autoReturnLicense

    /**
     * Sets the {@code autoReturnLicense} flag.
     * @param value {@code true} if the license should be auto returned
     * @return this
     */
    abstract T autoReturnLicense(Boolean value)

    /**
     * Returns a {@code true} if the Unity should be activated before the first Unity task starts executing.
     * @return {@code true} if the should be auto activated
     * @default true
     * @see wooga.gradle.unity.tasks.Activate
     */
    Boolean autoActivateUnity

    /**
     * Sets the {@code autoActivateUnity} flag.
     * @param value {@code true} if the should be auto activated
     * @return this
     */
    abstract T autoActivateUnity(Boolean value)

    /**
     * Returns the {@link wooga.gradle.unity.UnityAuthentication} object.
     * <p>
     * This object contains the user and serial credentials used to activate the Unity installation.
     * @return the Unity authentication credentials
     * @default empty credentials object
     */
    UnityAuthentication authentication

    /**
     * Sets the authentication object values with a {@link wooga.gradle.unity.UnityAuthentication} object.
     * <p>
     * The authentication values within the provided {@link wooga.gradle.unity.UnityAuthentication} object will be copied
     * to the internal {@link wooga.gradle.unity.UnityAuthentication authentication} object.
     * @param authentication
     * @return this
     */
    abstract T authentication(UnityAuthentication authentication)

    /**
     * Configures the {@link wooga.gradle.unity.UnityAuthentication authentication} object with a closure.
     * <p>
     * The closure configures a {@link wooga.gradle.unity.UnityAuthentication} object.
     *
     * @param closure The closure for configuring the authentication.
     * @return this
     */
    abstract T authentication(Closure closure)

    /**
     * Configures the {@link wooga.gradle.unity.UnityAuthentication authentication} object with an action.
     * <p>
     * The given action configures a {@link wooga.gradle.unity.UnityAuthentication} object.
     *
     * @param action The action for configuring the authentication.
     * @return this
     */
    abstract T authentication(Action<? super UnityAuthentication> action)
}