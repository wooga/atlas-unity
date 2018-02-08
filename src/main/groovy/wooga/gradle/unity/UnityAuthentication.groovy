package wooga.gradle.unity

import org.gradle.authentication.Authentication

interface UnityAuthentication extends Authentication {

    String getUsername()

    void setUsername(String username)

    String getPassword()

    void setPassword(String password)

    String getSerial()

    void setSerial(String serial)
}
