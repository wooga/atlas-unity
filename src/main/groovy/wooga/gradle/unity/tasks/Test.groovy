/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package wooga.gradle.unity.tasks

import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.*
import org.gradle.util.GUtil
import wooga.gradle.unity.batchMode.BatchModeFlags
import org.gradle.internal.Factory
import javax.inject.Inject

class Test extends AbstractUnityTask {

    private final List<Object> filter = new ArrayList()
    private final List<Object> categories = new ArrayList()
    private final FileResolver fileResolver

    private Factory<File> reportsPath

    @Internal
    @Optional
    File getReportsPath() {
        reportsPath.create()
    }

    void setReportsPath(Object path) {
        reportsPath = fileResolver.resolveLater(path)
    }

    Test reportsPath(Object path) {
        reportsPath = fileResolver.resolveLater(path)
        this
    }

    @Input
    @Optional
    List<String> getCategories() {
        List<String> args = new ArrayList<String>()
        for (Object argument : categories) {
            args.add(argument.toString())
        }
        return args
    }

    Test categories(Object... var1) {
        if (var1 == null) {
            throw new IllegalArgumentException("categories == null!")
        } else {
            this.categories.addAll(Arrays.asList(var1))
            return this
        }
    }

    Test categories(Iterable<?> var1) {
        GUtil.addToCollection(categories, var1)
        return this
    }

    Test setCategories(Iterable<?> var1) {
        this.filter.clear()
        GUtil.addToCollection(categories, var1)
        return this
    }

    @Input
    @Optional
    List<String> getFilter() {
        List<String> args = new ArrayList<String>()
        for (Object argument : filter) {
            args.add(argument.toString())
        }
        return args
    }

    Test filter(Object... var1) {
        if (var1 == null) {
            throw new IllegalArgumentException("filter == null!")
        } else {
            this.filter.addAll(Arrays.asList(var1))
            return this
        }
    }

    Test filter(Iterable<?> var1) {
        GUtil.addToCollection(filter, var1)
        return this
    }

    Test setFilter(Iterable<?> var1) {
        this.filter.clear()
        GUtil.addToCollection(filter, var1)
        return this
    }

    @Console
    boolean verbose = true

    @Console
    boolean teamcity = false

    @Inject
    Test(FileResolver fileResolver) {
        super(Test.class)
        this.fileResolver = fileResolver
        description = "Executes Unity in batch mode and executes specified method"
    }

    @TaskAction
    @Override
    protected void exec() {
        def testArgs = []

        if (reportsPath == null) {
            reportsPath = fileResolver.resolveLater("${project.buildDir}/reports/${name}-results.xml")
        }

        testArgs << BatchModeFlags.RUN_EDITOR_TESTS
        testArgs << BatchModeFlags.EDITOR_TEST_RESULTS_FILE << getReportsPath().path

        if (verbose) {
            testArgs << BatchModeFlags.EDITOR_TEST_VERBOSE_LOG
            if (teamcity) {
                testArgs << 'teamcity'
            }
        }

        if (filter.size() > 0) {
            testArgs << BatchModeFlags.EDITOR_TEST_FILTER << filter.join(",")
        }

        if (categories.size() > 0) {
            testArgs << BatchModeFlags.EDITOR_TEST_CATEGORIES << categories.join(",")
        }

        args(testArgs)
        super.exec()
    }
}
