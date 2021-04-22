package wooga.gradle.unity.traits

import org.gradle.api.Action
import org.gradle.api.tasks.Nested
import wooga.gradle.unity.models.UnityAuthentication

import static org.gradle.util.ConfigureUtil.configureUsing

trait UnityAuthenticationSpec {

    UnityAuthentication authentication
    @Nested
    UnityAuthentication getAuthentication(){
        authentication
    }
    void setAuthentication(UnityAuthentication value){
        authentication = value
    }

    void authentication(Closure closure) {
        authentication(configureUsing(closure))
    }

    void authentication(Action<? super UnityAuthentication> action) {
        action.execute(authentication)
    }

}
