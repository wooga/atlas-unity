package wooga.gradle.unity.utils

import spock.lang.Specification
import spock.lang.Unroll

class ForkTextStreamSpec extends Specification {

    ForkTextStream forkStream
    static String testString = "Test text to fork"

    def setup() {
        forkStream = new ForkTextStream()
    }

    @Unroll
    def "forks outputs stream to #amount"() {
        given: "a stream to output to"
        List<File> files = []
        List<Writer> writer = []

        for (int i = 0; i < numberOfForks; i++) {
            def f = File.createTempFile("stream", "out_${i}")
            def w = new FileWriter(f,true)

            files.add(f)
            writer.add(w)

            forkStream.addWriter(w)
        }

        when:
        forkStream.text(testString)
        forkStream.text(testString)
        forkStream.text(testString)
        forkStream.endOfStream()

        then:
        files.each {
            it.text == "${testString}${testString}${testString}"
        }

        cleanup:
        writer.each {
            it.close()
        }

        where:
        numberOfForks << [1, 10]
        amount = numberOfForks > 1 ? "multiple streams" : "single stream"
    }

    def "closes all sub streams when fork stream gets closed"() {
        given: "a stream to output to"
        List<File> files = []
        List<Writer> writer = []

        for (int i = 0; i < numberOfForks; i++) {
            def f = File.createTempFile("stream", "out_${i}")
            def w = new PrintWriter(f)

            files.add(f)
            writer.add(w)

            forkStream.addWriter(w)
        }

        when:
        forkStream.text(testString)

        forkStream.endOfStream()

        forkStream.text(testString)
        forkStream.text(testString)
        forkStream.endOfStream()

        then:
        files.each {
            it.text == "${testString}"
        }

        cleanup:
        writer.each {
            it.close()
        }

        where:
        numberOfForks << [10]
    }

    def "removes sub streams when fork stream throws IO Exception"() {
        given: "a stream to output to"
        List<File> files = []
        List<Writer> writer = []

        for (int i = 0; i < numberOfForks; i++) {
            def f = File.createTempFile("stream", "out_${i}")
            def w = new FileWriter(f)

            files.add(f)
            writer.add(w)

            forkStream.addWriter(w)
        }

        when:
        forkStream.text(testString)
        forkStream.text(testString)

        writer[0].close()

        forkStream.text(testString)
        forkStream.endOfStream()

        then:

        files[0].text == "${testString}${testString}"
        files.subList(1, files.size()).every {
            it.text == "${testString}${testString}${testString}"
        }

        cleanup:
        writer.each {
            it.close()
        }

        where:
        numberOfForks = 10
    }

}
