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

import spock.lang.Specification
import spock.lang.Unroll

class NUnitReportNormalizerTest extends Specification {

    static final String UNITY_REPORT_5_6 = """
    <test-suite 
        type="TestSuite" 
        id="1000" 
        name="Wooga.Test" 
        fullname="Wooga.Test" 
        runstate="Runnable" 
        testcasecount="0" 
        result="Passed" 
        start-time="2017-08-25 13:57:41Z" 
        end-time="2017-08-25 13:57:41Z" 
        duration="0.005744" 
        total="0" 
        passed="1" 
        failed="0" 
        inconclusive="0" 
        skipped="0" 
        asserts="0">
        <test-case 
            id="1176" name="shouldAddCorrectly" 
            fullname="Tests.Specs.Core.Data.shouldAddCorrectly" 
            methodname="shouldAddCorrectly" 
            classname="Adder" 
            runstate="Runnable" 
            seed="1536878189" 
            result="Passed" 
            start-time="2017-09-04 15:08:00Z" 
            end-time="2017-09-04 15:08:00Z" 
            duration="0.008622" 
            asserts="2" />
    </test-suite>
    """.stripIndent().trim()

    static final String NUNIT_3_REPORT = """
    <test-run>
        ${UNITY_REPORT_5_6}
    </test-run>
    """.stripIndent().trim()

    static final String UNITY_REPORT_5_5 = """
    <!--This file represents the results of running a test suite-->
    <test-results
        name="Unity Tests"
        total="1"
        errors="0"
        failures="0"
        not-run="0"
        inconclusive="0"
        ignored="0"
        skipped="0"
        invalid="0"
        date="2017-09-04"
        time="17:01:44">
        <environment
            nunit-version="2.6.4-Unity"
            clr-version="2.0.50727.1433"
            os-version="Unix 16.6.0.0"
            platform="Unix"
            cwd="/Wooga.Tests"
            machine-name="Test.local"
            user="test"
            user-domain="Test.local"
            unity-version="5.5.3f1"
            unity-platform="OSXEditor"/>
        <culture-info
            current-culture="en-US" 
            current-uiculture="en-US" />
        <test-suite 
            name="Wooga.Test" 
            type="Assembly" 
            executed="True" 
            result="Success" 
            success="True" 
            time="19.446">
            <results>
                <test-case 
                    name="LogTests.Tests" 
                    executed="True" 
                    result="Success" 
                    success="True" 
                    time="0.012">
                </test-case>
            </results>
        </test-suite>
    </test-results>
    """.stripIndent().trim()

    File report

    Boolean checkRootNode(File file, String name) {
        def node = new XmlParser().parse(file)
        node.name() == name
    }

    void containsXMLDeclaration(File file) {
        assert file.readLines()[0] =~ /<\?xml version=("|')1.0("|') encoding=("|')UTF-8("|')\?>/
    }

    def setup() {
        report = File.createTempFile("testReport", ".xml")
        report.deleteOnExit()
        report << '<?xml version="1.0" encoding="UTF-8"?>'
    }

    def cleanup() {
        report.delete()
    }

    def "normalize broken test report from unity"() {
        given: "Test report produced by Unity 5.6 || 2017.1"
        report << UNITY_REPORT_5_6
        assert !checkRootNode(report, "test-run")

        when:
        def result = NUnitReportNormalizer.normalize(report)

        then:
        result == NUnitReportNormalizerResult.SUCCESS
        checkRootNode(report, "test-run")
        containsXMLDeclaration(report)
    }

    @Unroll
    def "Skips valid #reportType reports"() {
        given: "Test report produced by Unity 5.5"
        report << reportContent
        assert checkRootNode(report, expectedRootNode)

        when:
        def result = NUnitReportNormalizer.normalize(report)

        then:
        result == NUnitReportNormalizerResult.SKIPPED
        checkRootNode(report, expectedRootNode)
        containsXMLDeclaration(report)

        where:
        reportType  | reportContent    | expectedRootNode
        "NUnit 2.x" | UNITY_REPORT_5_5 | "test-results"
        "NUnit 3.x" | NUNIT_3_REPORT   | "test-run"
    }

    def "fails for other xml formats"() {
        given: "A bogus XML"
        report << """
        <someRoot>
            <someItem/>
            <someItem/>
            <someItem/>
        </someRoot>
        """.stripIndent().trim()

        and: "the content as text"
        def content = report.text

        when:

        def result = NUnitReportNormalizer.normalize(report)

        then:
        result == NUnitReportNormalizerResult.FAILED
        report.text == content
        containsXMLDeclaration(report)
    }
}
