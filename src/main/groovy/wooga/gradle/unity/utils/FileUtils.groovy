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

package wooga.gradle.unity.utils

import org.gradle.api.file.RegularFileProperty
import org.gradle.platform.base.Platform

class FileUtils {
    static void ensureFile(File file) {
        if(!file.exists()) {
            File parent = file.getParentFile()
            if (!parent.exists() && !parent.mkdirs()) {
                throw new IllegalStateException("Couldn't create dir: " + parent)
            }
            file.createNewFile()
        }
    }
    static void ensureFile(RegularFileProperty file) {
        ensureFile(file.get().asFile)
    }

    static String OS = System.getProperty("os.name").toLowerCase()
    static boolean isWindows() {
        return (OS.indexOf("win") >= 0)
    }

    static boolean isMac() {
        return (OS.indexOf("mac") >= 0)
    }

    static boolean isLinux() {
        return (OS.indexOf("linux") >= 0)
    }
}
