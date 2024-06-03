//package org.jenkinsci.plugins.snowflakecli;
//
//import static com.google.common.collect.Iterables.filter;
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.contains;
//import static org.hamcrest.Matchers.containsInAnyOrder;
//import static org.hamcrest.Matchers.containsString;
//import static org.hamcrest.Matchers.empty;
//import static org.hamcrest.Matchers.greaterThanOrEqualTo;
//import static org.hamcrest.Matchers.hasItems;
//import static org.hamcrest.Matchers.hasSize;
//import static org.hamcrest.Matchers.instanceOf;
//import static org.hamcrest.Matchers.is;
//import static org.hamcrest.core.IsNot.not;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.util.Arrays;
//import java.util.List;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.TemporaryFolder;
//import org.jvnet.hudson.test.Issue;
//import org.jvnet.hudson.test.JenkinsRule;
//
//import com.google.common.base.Function;
//import com.google.common.base.Joiner;
//import com.google.common.base.Predicate;
//import com.google.common.base.Predicates;
//import com.google.common.collect.Iterables;
//
//import hudson.FilePath;
//import hudson.Launcher;
//import hudson.Launcher.LocalLauncher;
//import hudson.model.AbstractBuild;
//import hudson.model.Computer;
//import hudson.model.FreeStyleBuild;
//import hudson.model.FreeStyleProject;
//import hudson.model.Label;
//import hudson.model.Node;
//import hudson.model.Result;
//import hudson.model.StreamBuildListener;
//import hudson.model.queue.QueueTaskFuture;
//import hudson.tasks.BuildWrapper.Environment;
//import hudson.util.ArgumentListBuilder;
//import hudson.util.ProcessTree;
//import hudson.util.ProcessTree.OSProcess;
//
//public class XvfbBuildWrapperTest extends BaseBuildWrapperTest {
//
//    @Rule
//    public JenkinsRule system = new JenkinsRule();
//
//    @Rule
//    public TemporaryFolder tempDir = new TemporaryFolder();
//
//    @Before
//    public void setupSnowflakeInstallations() throws IOException {
//        setupSnowflakeInstallations(system.jenkins, tempDir);
//    }
//
//    @Test
//    public void byDefaultBuildnumbersShouldBeBasedOnExecutorNumber() throws Exception {
//        system.jenkins.setNumExecutors(3);
//
//        final SnowflakeFreeStyleBuildWrapper snowflakeWrapper = new SnowflakeFreeStyleBuildWrapper();
//        snowflakeWrapper.setSnowflakeInstallation("working");
//
//        final FreeStyleProject project1 = createFreeStyleJob(system, "byDefaultBuildnumbersShouldBeBasedOnExecutorNumber1");
//        setupSnowflakeWrapperOn(project1, snowflakeWrapper);
//
//        final FreeStyleProject project2 = createFreeStyleJob(system, "byDefaultBuildnumbersShouldBeBasedOnExecutorNumber2");
//        setupSnowflakeWrapperOn(project2, snowflakeWrapper);
//
//        final FreeStyleProject project3 = createFreeStyleJob(system, "byDefaultBuildnumbersShouldBeBasedOnExecutorNumber3");
//        setupSnowflakeWrapperOn(project3, snowflakeWrapper);
//
//        final QueueTaskFuture<FreeStyleBuild> build1Result = project1.scheduleBuild2(0);
//        final QueueTaskFuture<FreeStyleBuild> build2Result = project2.scheduleBuild2(0);
//        final QueueTaskFuture<FreeStyleBuild> build3Result = project3.scheduleBuild2(0);
//
//        final FreeStyleBuild build1 = build1Result.get();
//        final FreeStyleBuild build2 = build2Result.get();
//        final FreeStyleBuild build3 = build3Result.get();
//
//        final int display1 = build1.getAction(XvfbEnvironment.class).displayName;
//        final int display2 = build2.getAction(XvfbEnvironment.class).displayName;
//        final int display3 = build3.getAction(XvfbEnvironment.class).displayName;
//
//        final List<Integer> displayNumbersUsed = Arrays.asList(display1, display2, display3);
//        assertThat("By default display numbers should be based on executor number, they were: " + displayNumbersUsed, displayNumbersUsed, containsInAnyOrder(1, 2, 3));
//    }
//
//    @Test
//    @Issue("JENKINS-32039")
//    public void shouldCreateCommandLineArgumentsWithoutScreenIfNotGiven() throws IOException {
//        final Xvfb xvfb = new Xvfb();
//        xvfb.setInstallationName("cmd");
//        xvfb.setDisplayName(42);
//        xvfb.setScreen(null);
//
//        final XvfbInstallation installation = new XvfbInstallation("cmd", "/usr/local/cmd-xvfb", null);
//
//        final File tempDirRoot = tempDir.getRoot();
//        final ArgumentListBuilder arguments = xvfb.createCommandArguments(installation, new FilePath(tempDirRoot), 42);
//
//        assertThat(arguments.toList(), contains("/usr/local/cmd-xvfb/Xvfb", ":42", "-fbdir", tempDirRoot.getAbsolutePath()));
//    }
//
//    @SuppressWarnings("unchecked")
//    @Test
//    public void shouldFailIfInstallationIsNotFound() throws Exception {
//        final Xvfb xvfb = new Xvfb();
//        xvfb.setInstallationName("nonexistant");
//
//        final FreeStyleProject project = createFreeStyleJob(system, "shouldFailIfInstallationIsNotFound");
//        setupXvfbOn(project, xvfb);
//
//        final QueueTaskFuture<FreeStyleBuild> buildResult = project.scheduleBuild2(0);
//
//        final FreeStyleBuild build = buildResult.get();
//
//        system.assertBuildStatus(Result.FAILURE, build);
//
//        final List<String> logLines = build.getLog(10);
//
//        assertThat("If no Xvfb installations defined for use log should note that", logLines, hasItems(containsString("No Xvfb installations defined, please define one in the configuration")));
//    }
//}
