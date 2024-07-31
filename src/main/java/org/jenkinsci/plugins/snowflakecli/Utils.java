/*
 * Copyright 2024 Snowflake Inc. 
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jenkinsci.plugins.snowflakecli;

import hudson.FilePath;
import jenkins.model.Jenkins;

import java.io.IOException;

public class Utils {
    public static String[] getCommandCall(FilePath script) {
        return new String[]{"sh", "-e", script.getRemote()};
    }
    
    public static String getClassResourceContent(Class inputClass, String resourceName) throws IOException {
        String packageName = inputClass.getPackageName();
        String className = inputClass.getSimpleName();
        String scriptPath = packageName.replace(".", "/") + "/" + className + "/" + resourceName;
        byte[] encodedFile = Jenkins.getInstanceOrNull().pluginManager.uberClassLoader.getResourceAsStream(scriptPath).readAllBytes();
        return new String(encodedFile, "UTF-8");
    }
}
