package org.jenkinsci.plugins.snowflakecli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.JenkinsRule;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildWrapper;
import hudson.tools.ToolInstallation;
import hudson.util.DescribableList;
import jenkins.model.Jenkins;

public abstract class BaseBuildWrapperTest {

    protected static FreeStyleProject createFreeStyleJob(final JenkinsRule system, final String name) throws IOException {
        return system.jenkins.createProject(FreeStyleProject.class, name);
    }

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

    protected static FreeStyleBuild runFreestyleJobWith(final JenkinsRule system, final SnowflakeCLIBuildWrapperBase snowBuildWrapper) throws IOException, Exception {
        final FreeStyleProject project = createFreeStyleJob(system, "xvfbFreestyleJob");
        setupSnowflakeWrapperOn(project, snowBuildWrapper);

        return system.buildAndAssertSuccess(project);
    }

    protected static void setupSnowflakeWrapperOn(final FreeStyleProject project, final SnowflakeCLIBuildWrapperBase snowBuildWrapper) {
        final DescribableList<BuildWrapper, Descriptor<BuildWrapper>> buildWrappers = project.getBuildWrappersList();

        buildWrappers.add(snowBuildWrapper);
    }

    protected void setupSnowflakeInstallations(final Jenkins jenkins, final TemporaryFolder tempDir) throws IOException {
        final SnowflakeInstallation.DescriptorImpl installations = new SnowflakeInstallation.DescriptorImpl();

        installations.setInstallations(createInstallation("working", tempDir), createInstallation("failing", tempDir));

        final DescriptorExtensionList<ToolInstallation, Descriptor<ToolInstallation>> toolInstallations = jenkins.getDescriptorList(ToolInstallation.class);
        toolInstallations.add(installations);
    }
}