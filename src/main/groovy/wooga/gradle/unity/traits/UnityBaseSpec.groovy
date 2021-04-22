package wooga.gradle.unity.traits

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory
import org.gradle.internal.impldep.org.eclipse.jgit.errors.NotSupportedException

import javax.inject.Inject

trait UnityBaseSpec {

    @Inject
    ProviderFactory getProviderFactory() {
        throw new NotSupportedException("")
    }

    @Inject
    ObjectFactory getObjects() {
        throw new NotSupportedException("")
    }

}
