package wooga.gradle.unity.tasks.internal

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult
import org.gradle.process.internal.ExecException
import wooga.gradle.unity.batchMode.BaseBatchModeSpec
import wooga.gradle.unity.batchMode.BatchModeAction
import wooga.gradle.unity.batchMode.BatchModeSpec
import wooga.gradle.unity.batchMode.BuildTarget

abstract class AbstractUnityProjectTask<T extends AbstractUnityProjectTask> extends AbstractUnityTask implements BatchModeSpec {

    private interface ExecuteExclude {
        ExecResult execute() throws ExecException
    }

    @Delegate(excludeTypes = [ExecuteExclude.class], interfaces = false)
    protected BatchModeAction batchModeAction

    private ExecResult batchModeResult

    AbstractUnityProjectTask(Class taskType) {
        super(taskType)
        this.batchModeAction = retrieveBatchModeActionFactory().create()
    }

    @Override
    protected BaseBatchModeSpec retrieveAction() {
        return batchModeAction
    }

    @TaskAction
    protected void exec() {
        batchModeResult = batchModeAction.execute()
    }

    /**
     * {@inheritDoc}
     */
    @Optional
    @Input
    List<String> getArgs() {
        return batchModeAction.getArgs()
    }

    @Optional
    @Input
    @Override
    BuildTarget getBuildTarget() {
        batchModeAction.buildTarget
    }

    @Optional
    @Input
    @Override
    Boolean getQuit() {
        return batchModeAction.quit
    }

    @Optional
    @Input
    @Override
    Boolean getBatchMode() {
        return batchModeAction.batchMode
    }

    @Optional
    @Input
    @Override
    Boolean getNoGraphics() {
        batchModeAction.noGraphics
    }

    /**
     * {@inheritDoc}
     */
    @Input
    boolean isIgnoreExitValue() {
        return batchModeAction.isIgnoreExitValue()
    }
}
