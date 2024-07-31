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

import hudson.model.Result;
import hudson.model.queue.QueueTaskFuture;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.JenkinsRule;
import java.io.IOException;

public class SnowflakeBuildWrapperTest extends BaseBuildWrapperTest {
    
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();
    
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();
    
    @Before
    public void setupSnowflakeInstallations() throws IOException {
        setupSnowflakeInstallations(jenkins.jenkins, tempDir);
    }

    @Test
    public void settingConfigFile() throws Exception {
        WorkflowJob project = jenkins.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition("" +
            "node {" 
                + "  def root = tool type: 'snowflakecli', name: 'working'\n"
                + "  writeFile text: 'CONNECTION_STRING', file: 'config.toml'\n"
                + "  withEnv([\"PATH+SNOWFLAKECLI=${root}\"]) {\n"
                + "     wrap([$class: 'SnowflakeCLIBuildWrapper', snowflakeInstallation: 'working', configFilePath: 'config.toml']) {\n"
                + "         sh 'snowTemp connection list'\n"
                + "     }"
                +  "  }\n" 
                + "}", true));
        final QueueTaskFuture<WorkflowRun> buildResult = project.scheduleBuild2(0);
        final WorkflowRun build = buildResult.get();
        
        jenkins.assertBuildStatus(Result.SUCCESS, build);
        
        jenkins.assertLogContains("This executable will success", build);
    }
    
    @Test
    public void invalidConfigFile() throws Exception {
        WorkflowJob project = jenkins.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition("" +
            "node {" 
                + "  def root = tool type: 'snowflakecli', name: 'working'\n"
                + "  withEnv([\"PATH+SNOWFLAKECLI=${root}\"]) {\n"
                + "     wrap([$class: 'SnowflakeCLIBuildWrapper', snowflakeInstallation: 'working', configFilePath: 'config.toml']) {\n"
                + "         sh 'snowTemp connection list'\n"
                + "     }"
                +  "  }\n" 
                + "}", true));
        final QueueTaskFuture<WorkflowRun> buildResult = project.scheduleBuild2(0);
        final WorkflowRun build = buildResult.get();
        
        jenkins.assertBuildStatus(Result.FAILURE, build);
        
        jenkins.assertLogContains("Failed to create a temp directory on", build);
    }
    
    @Test
    public void installationNotFound() throws Exception {
        WorkflowJob project = jenkins.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition("" +
            "node {" 
                + "  def root = tool type: 'snowflakecli', name: 'invalid'\n"
                + "  withEnv([\"PATH+SNOWFLAKECLI=${root}\"]) {\n"
                + "     wrap([$class: 'SnowflakeCLIBuildWrapper', snowflakeInstallation: 'working', configFilePath: 'config.toml']) {\n"
                + "         sh 'snowTemp connection list'\n"
                + "     }"
                +  "  }\n" 
                + "}", true));
        final QueueTaskFuture<WorkflowRun> buildResult = project.scheduleBuild2(0);
        final WorkflowRun build = buildResult.get();
        
        jenkins.assertBuildStatus(Result.FAILURE, build);
        
        jenkins.assertLogContains("No snowflakecli named invalid found", build);
    }
    
    @Test
    public void failingExecutable() throws Exception {
        WorkflowJob project = jenkins.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition("" +
            "node {" 
                + "  def root = tool type: 'snowflakecli', name: 'failing'\n"
                + "  withEnv([\"PATH+SNOWFLAKECLI=${root}\"]) {\n"
                + "     wrap([$class: 'SnowflakeCLIBuildWrapper', snowflakeInstallation: 'failing', configFilePath: 'config.toml']) {\n"
                + "         sh 'snowTemp connection list'\n"
                + "     }"
                +  "  }\n" 
                + "}", true));
        final QueueTaskFuture<WorkflowRun> buildResult = project.scheduleBuild2(0);
        final WorkflowRun build = buildResult.get();
        
        jenkins.assertBuildStatus(Result.FAILURE, build);
        
        jenkins.assertLogContains("script returned exit code 1", build);
    }
}