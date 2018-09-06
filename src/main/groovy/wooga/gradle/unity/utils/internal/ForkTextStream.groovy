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

package wooga.gradle.unity.utils.internal

import org.gradle.api.Nullable
import org.gradle.internal.io.TextStream

class ForkTextStream implements TextStream {

    private final List<Writer> writerList = []

    void addWriter(Writer writer) {
        this.writerList.add(writer)
    }

    @Override
    void text(String text) {
        List<Writer> writersToRemove = []
        writerList.each {
            try {
                it.write(text)
            }
            catch (IOException ignored) {
                writersToRemove.add(it)
            }
        }

        writerList.removeAll(writersToRemove)
    }

    @Override
    void endOfStream(@Nullable Throwable failure) {
        writerList.each {
            try {
                it.close()
            }
            finally {

            }
        }
    }
}
