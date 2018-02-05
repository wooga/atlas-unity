package wooga.gradle.unity.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import wooga.gradle.unity.batchMode.BatchModeSpec
import wooga.gradle.unity.batchMode.BuildTarget

abstract class AbstractBatchModeTask extends AbstractUnityTask implements BatchModeSpec {

    AbstractBatchModeTask(Class taskType) {
        super(taskType)
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
}
