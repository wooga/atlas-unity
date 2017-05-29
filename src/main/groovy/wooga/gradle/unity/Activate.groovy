/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package wooga.gradle.unity

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

//class Activate extends AbstractUnityTask {
//
//    @Input
//    String serial
//
//    @Optional
//    @Input
//    String username
//
//    @Optional
//    @Input
//    String password
//
//
//    Activate() {
//        super(Activate.class)
//    }
//
//    @Override
//    protected List<String> configureArguments() {
//        UnityPluginExtension extension = project.extensions.getByType(UnityPluginExtension)
//        def args = []
//
//        if(serial == null) {
//            serial = extension.serial
//        }
//
//        if(username == null) {
//            username = extension.username
//        }
//
//        if(password == null) {
//            password = extension.password
//        }
//
//        args << "-serial" << serial
//        args << "-username" << username
//        args << "-password" << password
//    }
//}
