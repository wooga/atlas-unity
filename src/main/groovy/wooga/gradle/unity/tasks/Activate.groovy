package wooga.gradle.unity.tasks


import wooga.gradle.unity.UnityTask
import wooga.gradle.unity.models.DefaultUnityAuthentication
import wooga.gradle.unity.models.UnityCommandLineOption
import wooga.gradle.unity.traits.UnityAuthenticationSpec

/**
 * Activates a Unity installation with unity account and a serial number.
 * Example:
 * <pre>
 * {@code
 *     task activateUnity(type:wooga.gradle.unity.tasks.Activate) {*         authentication {*             username = "user@something.com"
 *             password = "thePassword"
 *             serial = "unitySerialNumber"
 *}*}*}
 * </pre>
 *
 * Make sure that license file folder exists, and has appropriate permissions before running this task.
 */
class Activate extends UnityTask implements UnityAuthenticationSpec {

    Activate() {
        authentication = new DefaultUnityAuthentication(project.objects)
        onlyIf({

            // If all 3 haven't been assigned, don't throw since this is optional
            if (!authentication.username.isPresent()
                    && !authentication.password.isPresent()
                    && !authentication.serial.isPresent()) {
                logger.warn("No authentication fields have been set")
                return false
            }

            if (!authentication.username.get()) {
                throw new Exception("The username is missing")
            }

            if (!authentication.password.get()) {
                throw new Exception("The password is missing")
            }
            if (!authentication.serial.get()) {
                throw new Exception("The serial is missing")
            }

            return true
        })
    }

    @Override
    protected void preExecute() {
        super.preExecute()
        // @TODO: When moved to constructor, doesn't get queried properly. It could be because the authentication keeps changing?
        setCommandLineOptionConvention(UnityCommandLineOption.userName, authentication.username)
        setCommandLineOptionConvention(UnityCommandLineOption.password, authentication.password)
        setCommandLineOptionConvention(UnityCommandLineOption.serial, authentication.serial)
    }
}
