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

import groovy.xml.StreamingMarkupBuilder
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.xml.sax.SAXParseException

import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory
import java.nio.file.Files
import java.nio.file.StandardCopyOption

enum NUnitReportNormalizerResult {
    SKIPPED, SUCCESS, FAILED
}

class NUnitReportNormalizer {

    static Logger logger = Logging.getLogger(NUnitReportNormalizer)

    private static final NUnit3Root = "test-run"

    private static final NUnit2Schema = "nunit_25_report.xslt"
    private static final NUnit3Schema = "nunit_30_report_partial.xslt"

    private File reportFile

    NUnitReportNormalizer(File reportFile) {
        this.reportFile = reportFile
    }

    NUnitReportNormalizerResult normalize() {

        if (validNUnit(reportFile, NUnit2Schema)
                || validNUnit(reportFile, NUnit3Schema)) {
            logger.debug("normalization skipped for file ${reportFile.path}")
            return NUnitReportNormalizerResult.SKIPPED
        }

        Node reportRoot = new XmlParser().parse(reportFile)
        reportRoot.toString()

        if (reportRoot.name() != NUnit3Root) {
            def normalized = new Node(null, NUnit3Root)
            normalized.append(reportRoot)

            def normalizedReport = File.createTempFile(reportFile.name, "", reportFile.parentFile)

            normalizedReport.withWriter('UTF-8') { out ->
                out << new StreamingMarkupBuilder().bind { mkp.xmlDeclaration(version: "1.0", encoding: "UTF-8") }
                new XmlNodePrinter(new PrintWriter(out), "    ").print(normalized)
            }

            if (!validNUnit(normalizedReport, NUnit3Schema)) {
                logger.debug("normalization failed for file ${reportFile.path}")
                return NUnitReportNormalizerResult.FAILED
            }

            Files.move(normalizedReport.toPath(), reportFile.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        }

        logger.debug("normalization success for file ${reportFile.path}")
        return NUnitReportNormalizerResult.SUCCESS
    }

    private static Boolean validNUnit(File report, String schema) {
        InputStream xslt = NUnitReportNormalizer.class.getResourceAsStream("/schema/$schema")
        StreamSource s = new StreamSource(new FileReader(report))

        try {
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                    .newSchema(new StreamSource(xslt))
                    .newValidator()
                    .validate(s)
        }
        catch (SAXParseException parseException) {
            logger.debug("parse exception in file ${report.path}: ${parseException.message}")
            return false
        }
        return true
    }

    static NUnitReportNormalizerResult normalize(File report) {
        new NUnitReportNormalizer(report).normalize()
    }
}
