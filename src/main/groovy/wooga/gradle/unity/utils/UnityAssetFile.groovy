/*
 * Copyright 2021 Wooga GmbH
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

import groovyjarjarcommonscli.MissingArgumentException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

/**
 * Manages the parsing and serialization of an Unity YAML asset file
 * https://docs.unity3d.com/Manual/UnityYAML.html
 */
abstract class UnityAssetFile {

    static Logger logger = Logging.getLogger(UnityAssetFile)

    private final validObject
    private final File assetFile
    private final UnityAssetFileText assetFileText

    private Map<String, Object> content = [:]

    UnityAssetFile(File assetFile) {
        this(assetFile.text)
        this.assetFile = assetFile
    }

    UnityAssetFile(String content) {
        boolean isValid = false
        try {
            Yaml parser = new Yaml()
            assetFileText = strip(content)
            Map<String, Object> c = parser.load(assetFileText.stripped)
            this.content = c[c.keySet().first()] as Map<String, Object>
            isValid = true

        }
        catch (Exception e) {
            logger.warn("UnityAssetFile content not parsable (Was it set to binary?):")
            logger.warn(e.toString())
        }
        validObject = isValid
    }

    @Override
    int hashCode() {
        return content.hashCode()
    }

    @Override
    boolean equals(Object obj) {
        if (!obj) {
            return false
        }
        if (obj instanceof UnityAssetFile) {
            UnityAssetFile other = (UnityAssetFile)obj
            return this.content == other.content
        }
        return false
    }

    Boolean isSerialized() {
        assetFile != null
    }

    Boolean isValid() {
        validObject
    }

    File getAssetFile(){
        assetFile
    }

    Map<String, Object> getContent() {
        content
    }

    boolean write() {
        write(null)
    }

    boolean write(File assetFile, indent = 2){

        if (assetFile == null){
            if (isSerialized()){
                assetFile = this.assetFile
            }
            else{
                throw new MissingArgumentException("No asset file was either set or provided to write to")
            }
        }

        def options = new DumperOptions()
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
        options.indent = indent

        Yaml parser = new Yaml(options)

        def lines = []

        // Append the yaml header and unity's instructions
        lines.addAll(assetFileText.instructions)

        // Add the main map
        def property = ["PlayerSettings" : content]
        def contentAsYaml = parser.dump(property)
        contentAsYaml = contentAsYaml.replaceAll(": null\n", ": \n")
        lines.add(contentAsYaml)

        // Add a newline at the end of the file
        lines.add("\n")

        def serialization = lines.join("\n")
        assetFile.write(serialization)
        return true
    }

    static UnityAssetFileText strip(String content) {
        UnityAssetFileText parse = new UnityAssetFileText(content)
        return parse
    }
}

class UnityAssetFileText {
    final String original
    final String stripped
    final List<String> instructions

    UnityAssetFileText(String content) {
        original = content
        def lines = content.readLines()
        lines.removeAll({ it -> it.isEmpty()})
        instructions = lines[0,1,2]
        lines[1] = ""
        lines[2] = "--- "
        stripped = lines.join("\n")
    }
}
