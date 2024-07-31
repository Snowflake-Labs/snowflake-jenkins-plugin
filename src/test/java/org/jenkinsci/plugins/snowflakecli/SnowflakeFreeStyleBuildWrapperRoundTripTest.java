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

import hudson.model.FreeStyleProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;

public class SnowflakeFreeStyleBuildWrapperRoundTripTest extends BaseBuildWrapperTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Before
    public void setupSnowflakeInstallations() throws IOException {
        setupSnowflakeInstallations(jenkins.jenkins, tempDir);
    }
    
    @Test
    public void testFileMode() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        SnowflakeCLIFreeStyleBuildWrapper buildWrapper1 = new SnowflakeCLIFreeStyleBuildWrapper();
        SnowflakeCLIFreeStyleBuildWrapper buildWrapper2 = new SnowflakeCLIFreeStyleBuildWrapper();
        Configuration configuration = new Configuration("file", null, "config.toml");
        buildWrapper1.setConfig(configuration);
        buildWrapper1.setSnowflakeInstallation("working");
        buildWrapper2.setConfig(configuration);
        buildWrapper2.setSnowflakeInstallation("working");
        project.getBuildWrappersList().add(buildWrapper1);
        project = jenkins.configRoundtrip(project);
        
        jenkins.submit(jenkins.createWebClient().getPage(project, "configure").getFormByName("config"));
        
        jenkins.assertEqualDataBoundBeans(
                buildWrapper2, project.getBuildWrappersList().get(0));
    }
    
    @Test
    public void testInlineMode() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        SnowflakeCLIFreeStyleBuildWrapper buildWrapper1 = new SnowflakeCLIFreeStyleBuildWrapper();
        SnowflakeCLIFreeStyleBuildWrapper buildWrapper2 = new SnowflakeCLIFreeStyleBuildWrapper();
        Configuration configuration = new Configuration("inline", "CONNECTION_TEXT", null);
        buildWrapper1.setConfig(configuration);
        buildWrapper1.setSnowflakeInstallation("working");
        buildWrapper2.setConfig(configuration);
        buildWrapper2.setSnowflakeInstallation("working");
        project.getBuildWrappersList().add(buildWrapper1);
        project = jenkins.configRoundtrip(project);
        
        jenkins.submit(jenkins.createWebClient().getPage(project, "configure").getFormByName("config"));
        
        jenkins.assertEqualDataBoundBeans(
                buildWrapper2, project.getBuildWrappersList().get(0));
    }
}