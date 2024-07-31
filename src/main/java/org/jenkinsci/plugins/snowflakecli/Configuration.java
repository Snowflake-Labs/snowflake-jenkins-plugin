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

import org.kohsuke.stapler.DataBoundConstructor;

public class Configuration {
    private final String value;
    private final String fileConfig;
    private final String inlineConfig;
    private Mode mode;

    public enum Mode {
        INLINE, FILE, INVALID
    }

    @DataBoundConstructor
    public Configuration(String value, String inlineConfig, String fileConfig) {
        this.value = value;
        this.fileConfig = fileConfig;
        this.inlineConfig = inlineConfig;
        this.mode = getMode(value);
    }
    
    public Mode getMode() {
        return this.mode;
    }
    
    public String getInlineConfig() {
        return this.inlineConfig;
    }
    
    public String getFileConfig() {
        return this.fileConfig;
    }
    
    public String getValue() {
        return this.value;
    } 
    
    private Mode getMode(String mode)
    {
        mode = mode.trim().toUpperCase();
        try {
            return Mode.valueOf(mode);
        }
        catch (IllegalArgumentException e) {
            return Mode.INVALID;
        }
    }
}