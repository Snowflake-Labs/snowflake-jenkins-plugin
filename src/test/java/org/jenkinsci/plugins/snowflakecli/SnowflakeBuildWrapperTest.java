package org.jenkinsci.plugins.snowflakecli;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import hudson.model.Result;
import hudson.model.queue.QueueTaskFuture;
import hudson.scm.SCM;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.RestartableJenkinsRule;
import org.jvnet.hudson.test.SingleFileSCM;

import java.io.File;
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
    public void inlineConfigurationMode() throws Exception {
        WorkflowJob project = jenkins.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition("" +
            "node {" 
                + "  wrap([$class: 'SnowflakeBuildWrapper', snowflakeInstallation: 'working', configFilePath: 'config.toml']) {\n"
                + "    sh 'snowTemp connection list'\n"
                + "  }\n" +
            "}", true));
        final QueueTaskFuture<WorkflowRun> buildResult = project.scheduleBuild2(0);
        final WorkflowRun build = buildResult.get();
        
        jenkins.assertBuildStatus(Result.SUCCESS, build);
        
        jenkins.assertLogContains("writeFile", build);
    }
}