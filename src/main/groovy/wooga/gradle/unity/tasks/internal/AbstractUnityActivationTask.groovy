package wooga.gradle.unity.tasks.internal

import org.gradle.api.internal.ConventionMapping
import org.gradle.api.tasks.Input
import org.gradle.internal.Factory
import org.gradle.process.ExecResult
import org.gradle.process.internal.ExecException
import wooga.gradle.unity.batchMode.ActivationAction
import wooga.gradle.unity.batchMode.BaseBatchModeSpec
import wooga.gradle.unity.internal.UnityPluginActionExtension

abstract class AbstractUnityActivationTask<T extends AbstractUnityActivationTask> extends AbstractUnityTask {

    @Override
    protected BaseBatchModeSpec retrieveAction() {
        return activationAction
    }

    @Override
    ConventionMapping getConventionMapping() {
        return activationAction.conventionMapping
    }

    interface ExecuteExclude {
        ExecResult activate() throws ExecException

        ExecResult returnLicense() throws ExecException
    }

    @Delegate(excludeTypes = [ExecuteExclude.class], interfaces = false)
    protected ActivationAction activationAction

    AbstractUnityActivationTask(Class taskType) {
        super(taskType)
        this.activationAction = retrieveActivationActionFactory().create()
    }

    protected Factory<ActivationAction> retrieveActivationActionFactory() {
        return project.extensions.getByType(UnityPluginActionExtension).activationActionFactory
    }

    /**
     * Tells whether a non-zero exit value is ignored, or an exception thrown. Defaults to <code>false</code>.
     *
     * @return whether a non-zero exit value is ignored, or an exception thrown
     */
    @Input
    boolean isIgnoreExitValue() {
        return activationAction.isIgnoreExitValue()
    }
}
