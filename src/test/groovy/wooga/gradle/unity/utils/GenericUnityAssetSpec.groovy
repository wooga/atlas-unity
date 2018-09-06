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

import spock.lang.Unroll
import wooga.gradle.unity.utils.internal.UnityAssetFile
import wooga.gradle.unity.utils.internal.UnityAssetFileSpec

class GenericUnityAssetSpec extends UnityAssetFileSpec {

    static String ASSET = """      
    %YAML 1.1
    %TAG !u! tag:unity3d.com,2011:
    --- !u!129 &1
    CustomObject:
        integerKey: 0
        nullKey:
        stringKey: 'a string with whitespace'
        listKey:
            - 'a value'
            - 'another value'
    """.stripIndent()

    @Override
    Class<UnityAssetFile> getClassImp() {
        return GenericUnityAsset
    }

    @Unroll
    def "can access content by key"() {
        when:
        def asset = new GenericUnityAsset(content)

        then:
        asset[key] == expectedValue

        where:
        key          | expectedValue
        'integerKey' | 0
        'nullKey'    | null
        'stringKey'  | 'a string with whitespace'
        'listKey'    | ['a value', 'another value']
        content = ASSET
    }

    @Unroll
    def "responds to Map method `#method`"() {
        given: "a asset object"
        def asset = new GenericUnityAsset(content)

        when:
        def result = asset.invokeMethod(method, parameter)

        then:
        def e = expectedReturnValue
        result == e

        where:
        method          | expectedReturnValue                                                                                                             | parameter
        "size"          | 4                                                                                                                               | null
        "isEmpty"       | false                                                                                                                           | null
        "containsKey"   | true                                                                                                                            | 'nullKey'
        "containsValue" | true                                                                                                                            | 'a string with whitespace'
        "get"           | 'a string with whitespace'                                                                                                      | 'stringKey'
        "put"           | null                                                                                                                            | ['newKey', 'value']
        "put"           | 'a string with whitespace'                                                                                                      | ['stringKey', 'value']
        "remove"        | 'a string with whitespace'                                                                                                      | 'stringKey'
        "putAll"        | null                                                                                                                            | ['key': 'value']
        "keySet"        | ['integerKey', 'nullKey', 'stringKey', 'listKey'].toSet()                                                                       | null
        "entrySet"      | ['integerKey': 0, 'nullKey': null, 'stringKey': 'a string with whitespace', 'listKey': ['a value', 'another value']].entrySet() | null

        content = ASSET
    }

    def "responds to Map method `clear`"() {
        given: "a asset object"
        def asset = new GenericUnityAsset(content)
        assert !asset.isEmpty()

        when:
        asset.clear()

        then:
        asset.isEmpty()

        where:
        content = ASSET
    }

    def "responds to Map method `values`"() {
        given: "a asset object"
        def asset = new GenericUnityAsset(content)

        when:
        def result = asset.values()

        then:
        def e = expectedReturnValue
        result.toList() == e.toList()

        where:
        expectedReturnValue = ['integerKey': 0, 'nullKey': null, 'stringKey': 'a string with whitespace', 'listKey': ['a value', 'another value']].values()
        content = ASSET
    }
}
