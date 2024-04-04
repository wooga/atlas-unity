package wooga.gradle.unity.internal

class ForkedOutputStream extends OutputStream {

    final OutputStream first
    final OutputStream second

    ForkedOutputStream(OutputStream first, OutputStream second) {
        this.first = first
        this.second = second
    }

    @Override
    void write(int i) throws IOException {
        first.write(i)
        second.write(i)
    }

    @Override
    void close() throws IOException {
        super.close()
        first.close()
        second.close()
    }
}