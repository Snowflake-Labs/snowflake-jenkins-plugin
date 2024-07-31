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

package org.jenkinsci.plugins.snowflakecli.SnowflakeBuildWrapper
f = namespace('/lib/form')


f.block() {
    f.div(style: "margin: 0px 0px") {
        f.table(style: "width: 100%") {
            f.entry(field: 'snowflakeInstallation', title: _('Select installer')) {
                f.select();
            }
            
            f.radioBlock(checked: descriptor.isInlineConfigChecked(instance), name: 'config', value: 'inline', title: 'Configuration Text') {
                f.entry(title: 'Snowflake CLI Text Configuration', field: 'inlineConfig', description: 'Inline configuration') {
                    f.textarea();
                }
            }
            f.radioBlock(checked: descriptor.isFileConfigChecked(instance), name: 'config', value: 'file', title: 'Configuration Path') {
                f.entry(title: 'Snowflake CLI File Configuration', field: 'fileConfig', description: 'Relative Path to the configuration file') {
                    f.textbox();
                }
            }
        }
    }
}