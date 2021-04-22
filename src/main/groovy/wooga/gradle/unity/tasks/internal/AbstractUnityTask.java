/*
 * Copyright 2018 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package wooga.gradle.unity.tasks.internal;

import groovy.lang.Closure;
import org.apache.commons.lang3.ObjectUtils;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.internal.ConventionMapping;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.internal.IConventionAware;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.internal.Factory;
import org.gradle.process.ExecResult;
import wooga.gradle.unity.batchMode.BaseBatchModeSpec;
import wooga.gradle.unity.batchMode.BatchModeAction;
import wooga.gradle.unity.internal.UnityPluginActionExtension;

import javax.annotation.Nullable;
import java.io.File;
import java.util.concurrent.Callable;

abstract class AbstractUnityTask<T extends AbstractUnityTask> extends ConventionTask implements BaseBatchModeSpec, ConventionMapping {

    private static Logger logger = Logging.getLogger(AbstractUnityTask.class);

    private final Class<T> taskType;

    abstract protected BaseBatchModeSpec retrieveAction();

    protected Factory<BatchModeAction> retrieveBatchModeActionFactory() {
        return retrieveUnityActionExtension().getBatchModeActionFactory();
    }

    protected UnityPluginActionExtension retrieveUnityActionExtension() {
        return getProject().getExtensions().getByType(UnityPluginActionExtension.class);
    }

    AbstractUnityTask(Class<T> taskType) {
        this.taskType = taskType;
    }

    public ConventionMapping getConventionMapping() {
        return this;
    }

    @Optional
    @Input
    @Override
    public File getUnityPath() {
        return retrieveAction().getUnityPath();
    }

    @Optional
    @Input
    @Override
    public File getProjectPath() {
        return retrieveAction().getProjectPath();
    }

    @Optional
    @Internal
    @Override
    public File getLogFile() {
        return retrieveAction().getLogFile();
    }

    @Optional
    @Input
    public String getLogCategory() {
        return retrieveAction().getLogCategory();
    }

    /**
     * Returns the result for the command run by this task. Returns {@code null} if this task has not been executed yet.
     *
     * @return The result. Returns {@code null} if this task has not been executed yet.
     */
    @Internal
    public ExecResult getBatchModeResult() {
        return getBatchModeResult();
    }

    protected ConventionMapping retrieveActionMapping() {
        return ((IConventionAware)this.retrieveAction()).getConventionMapping();
    }

    @Override
    public MappedProperty map(String propertyName, Closure<?> value) {

        MappedProperty result;
        try {
            result = retrieveActionMapping().map(propertyName, value);
        }
        catch (InvalidUserDataException ignored) {
            result = super.getConventionMapping().map(propertyName, value);
        }
        return result;
    }
    @Override
    MappedProperty map(String propertyName, Callable<?> value) {
        MappedProperty result;
        try {
            result = retrieveActionMapping().map(propertyName, value);
        }
        catch (InvalidUserDataException ignored) {
            result = super.getConventionMapping().map(propertyName, value)
        }
        return result;
    }

    @Nullable
    @Override
    public <T> T getConventionValue( @Nullable T actualValue, String propertyName, boolean isExplicitValue) {
        return retrieveActionMapping().getConventionValue(actualValue, propertyName, isExplicitValue) ?:
                super.getConventionMapping().getConventionValue(actualValue, propertyName, isExplicitValue);
    }
}
