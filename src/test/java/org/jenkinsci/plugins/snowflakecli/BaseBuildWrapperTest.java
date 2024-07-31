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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.rules.TemporaryFolder;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;
import hudson.tools.ToolInstallation;
import jenkins.model.Jenkins;

public abstract class BaseBuildWrapperTest {
    protected static SnowflakeInstallation createInstallation(final String nameAndPath, final TemporaryFolder tempDir) throws IOException, FileNotFoundException {
        final File snowflakeDir = tempDir.newFolder(nameAndPath);
        final File snowflakeFile = new File(snowflakeDir, "snowTemp");

        InputStream snowflakeFrom = null;
        FileOutputStream snowflakeTo = null;
        try {
            snowflakeFrom = BaseBuildWrapperTest.class.getResourceAsStream("/" + nameAndPath + "/snowTemp");
            snowflakeTo = new FileOutputStream(snowflakeFile);
            ByteStreams.copy(snowflakeFrom, snowflakeTo);
        } finally {
            Closeables.closeQuietly(snowflakeFrom);
            Closeables.close(snowflakeTo, true);
        }
        snowflakeFile.setExecutable(true);

        return new SnowflakeInstallation(nameAndPath, snowflakeDir.getAbsolutePath(), null);
    }

    protected void setupSnowflakeInstallations(final Jenkins jenkins, final TemporaryFolder tempDir) throws IOException {
        final SnowflakeInstallation.DescriptorImpl installations = new SnowflakeInstallation.DescriptorImpl();

        installations.setInstallations(createInstallation("working", tempDir), createInstallation("failing", tempDir));

        final DescriptorExtensionList<ToolInstallation, Descriptor<ToolInstallation>> toolInstallations = jenkins.getDescriptorList(ToolInstallation.class);
        toolInstallations.add(installations);
    }
}