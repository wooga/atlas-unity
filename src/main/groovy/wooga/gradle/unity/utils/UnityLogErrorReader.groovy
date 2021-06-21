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

import java.util.regex.Matcher

enum UnityLogErrorType {
    /**
     * No error was found
     */
    None,
    /**
     * The error could not be parsed
     */
    Unknown,
    /**
     * The Unity scripts have compiler errors
     */
    ScriptsCompilerError
}

class UnityLogErrorParse {
    UnityLogErrorType type
    String message
    List<CSharpFileCompilationResult> compilerOutput

    @Override
    String toString() {
        def result = "${type}:"
        switch (type) {
            case UnityLogErrorType.ScriptsCompilerError:
                compilerOutput.forEach({l ->
                    result += "\n${l.text}"
                })
                break
        }
        result
    }
}

class CSharpFileCompilationResult {
    String text
    String filePath
    Integer line
    Integer column
    String level
    String code
    String message

    CSharpFileCompilationResult(String filePath, Integer line, Integer column, String level, String code, String message) {
        this.filePath = filePath
        this.line = line
        this.column = column
        this.level = level
        this.code = code
        this.message = message
    }

    CSharpFileCompilationResult(String text, String filePath, Integer line, Integer column, String level, String code, String message) {
        this.text = text
        this.filePath = filePath
        this.line = line
        this.column = column
        this.level = level
        this.code = code
        this.message = message
    }

    CSharpFileCompilationResult() {
    }

    // e.g: "A/B.cs(9,7): warning CS0105: WRONG!"
    static String pattern = /(?<filePath>.+)\((?<line>\d+),(?<column>\d+)\):\s(?<level>.*?)\s(?<code>.+):\s(?<message>.*)/

    static CSharpFileCompilationResult Parse(String text) {
        Matcher matcher = (text =~ pattern)
        if (matcher.count == 0) {
            return null
        }

        def (all, filePath, line, column, level, code, message) = matcher[0]
        CSharpFileCompilationResult result = new CSharpFileCompilationResult()
        result.text = all
        result.filePath = filePath
        result.line = Integer.parseInt(line)
        result.column = Integer.parseInt(column)
        result.level = level
        result.code = code
        result.message = message
        result
    }
}

class UnityLogErrorReader {

    static String compilerOutputStartMarker = "-----CompilerOutput:"
    static String compilerOutputEndMarker = "-----EndCompilerOutput"
    static String compilerErrorMarker = "Scripts have compiler errors."
    static String errorMarker = "Aborting batchmode due to failure:"
    static String dialogError = "Cancelling DisplayDialog:"

    static UnityLogErrorParse readErrorMessageFromLog(File logfile) {

        UnityLogErrorParse parse = new UnityLogErrorParse()
        parse.type = UnityLogErrorType.None

        if (!logfile || !logfile.exists()) {
            return parse
        }

        boolean foundCompilerMarker = false
        boolean foundErrorMarker = false
        parse.compilerOutput = []
        String line
        Integer lineNumber = 0

        // Read through the log file
        logfile.withReader { reader ->
            while ((line = reader.readLine()) != null) {
                lineNumber++

                // If inside a marker, parse
                if (foundCompilerMarker) {
                    // Finished reading compiler output
                    if (line.startsWith(compilerOutputEndMarker)) {
                        foundCompilerMarker = false
                    }
                    // Record all warnings/errors
                    else {
                        CSharpFileCompilationResult fileCompilationResult = CSharpFileCompilationResult.Parse(line)
                        if (fileCompilationResult != null) {
                            parse.compilerOutput.add(fileCompilationResult)
                        }
                    }
                } else if (foundErrorMarker) {
                    if (line.startsWith(compilerErrorMarker)) {
                        parse.type = UnityLogErrorType.ScriptsCompilerError
                        break
                    } else {
                        parse.message = line
                    }
                }
                // Look for markers
                else {
                    // The error marker is found near the end of the log output
                    if (line.startsWith(errorMarker)) {
                        parse.type = UnityLogErrorType.Unknown
                        foundErrorMarker = true
                    }
                    // Started reading through C# compiler output
                    else if (line.startsWith(compilerOutputStartMarker)) {
                        foundCompilerMarker = true
                    }
//                    else if (line.startsWith(dialogError)) {
//                        parse.message = line.replace(dialogError, "").trim()
//                        break
//                    }
                }
            }
        }
        parse
    }
}
