package org.jenkinsci.plugins.snowflakecli;

import hudson.model.*;
import hudson.model.queue.QueueTaskFuture;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.SingleFileSCM;
import java.io.IOException;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;

public class SnowflakeFreeStyleBuildWrapperTest extends BaseBuildWrapperTest {
    
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
        SnowflakeCLIFreeStyleBuildWrapper buildWrapper = new SnowflakeCLIFreeStyleBuildWrapper();
        Configuration configuration = new Configuration("Inline", "CONNECTION_TEXT", "");
        buildWrapper.setConfig(configuration);
        buildWrapper.setSnowflakeInstallation("working");
        
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildWrappersList().add(buildWrapper);
        
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        
        jenkins.assertLogContains("Creating temporal config file in "+ build.getWorkspace() +"/config", build);
    }
    
    
    
    @Test
    public void fileConfigurationMode() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.setScm(new SingleFileSCM("config.toml", "CONNECTION_TEXT"));
        
        SnowflakeCLIFreeStyleBuildWrapper buildWrapper = new SnowflakeCLIFreeStyleBuildWrapper();
        Configuration configuration = new Configuration("File", "", "config.toml");
        buildWrapper.setConfig(configuration);
        buildWrapper.setSnowflakeInstallation("working");
        
        project.getBuildWrappersList().add(buildWrapper);
        
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        
        jenkins.assertLogContains("Setting config file to config.toml", build);
    }
    
    @Test
    public void fileConfigurationModeFileNotFound() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        SnowflakeCLIFreeStyleBuildWrapper buildWrapper = new SnowflakeCLIFreeStyleBuildWrapper();
        Configuration configuration = new Configuration("File", "", "unexisting.toml");
        buildWrapper.setConfig(configuration);
        buildWrapper.setSnowflakeInstallation("working");
        
        project.getBuildWrappersList().add(buildWrapper);
        
        final QueueTaskFuture<FreeStyleBuild> buildResult = project.scheduleBuild2(0);
        final FreeStyleBuild build = buildResult.get();
        
        jenkins.assertBuildStatus(Result.FAILURE, build);
        
        final List<String> logLines = build.getLog(100);
        
        assertThat("If file not found log should note that", logLines, hasItems(containsString(Messages.ConfigurationPathNotFound("unexisting.toml"))));
    }
    
    @Test
    public void invalidConfigurationMode() throws Exception {
        SnowflakeCLIFreeStyleBuildWrapper buildWrapper = new SnowflakeCLIFreeStyleBuildWrapper();
        Configuration configuration = new Configuration("Invalid", "CONNECTION_TEXT", "");
        buildWrapper.setConfig(configuration);
        buildWrapper.setSnowflakeInstallation("working");
        
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildWrappersList().add(buildWrapper);
        
        final QueueTaskFuture<FreeStyleBuild> buildResult = project.scheduleBuild2(0);
        final FreeStyleBuild build = buildResult.get();
        
        jenkins.assertBuildStatus(Result.FAILURE, build);
        
        final List<String> logLines = build.getLog(100);
        
        assertThat("If no valid configuration mode defined for use log should note that", logLines, hasItems(containsString(Messages.InvalidConfigMode())));
    }
    
    @Test
    public void installationNotFoundError() throws Exception {
        final SnowflakeCLIFreeStyleBuildWrapper snowflakeFreeStyleBuildWrapper = new SnowflakeCLIFreeStyleBuildWrapper();
        Configuration configuration = new Configuration("Inline", "CONNECTION_TEXT", "");
        snowflakeFreeStyleBuildWrapper.setSnowflakeInstallation("unexistant");
        snowflakeFreeStyleBuildWrapper.setConfig(configuration);
        
        final FreeStyleProject project = createFreeStyleJob(jenkins, "shouldFailIfInstallationIsNotFound");
        project.getBuildWrappersList().add(snowflakeFreeStyleBuildWrapper);
        
        final QueueTaskFuture<FreeStyleBuild> buildResult = project.scheduleBuild2(0);
        final FreeStyleBuild build = buildResult.get();
        
        jenkins.assertBuildStatus(Result.FAILURE, build);
        
        final List<String> logLines = build.getLog(10);
        
        assertThat("If no Snowflake installations defined for use log should note that", logLines, hasItems(containsString(Messages.InstallationNotFound())));
    }
}