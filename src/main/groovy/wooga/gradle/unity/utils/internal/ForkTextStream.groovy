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
