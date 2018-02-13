package wooga.gradle.unity

import org.gradle.authentication.Authentication

interface UnityAuthentication extends Authentication {

    /**
     * Returns the unity account username.
     * @return the username
     */
    String getUsername()

    /**
     * Sets the unity account username.
     * @param username the username
     */
    void setUsername(String username)

    /**
     * Returns the unity account password.
     * @return the password
     */
    String getPassword()

    /**
     * Sets the unity account password.
     * @param password the password
     */
    void setPassword(String password)

    /**
     * Returns the Unity serial number.
     * @return the serial number
     */
    String getSerial()

    /**
     * Sets the Unity serial number.
     * @param serial the serial number
     */
    void setSerial(String serial)
}
