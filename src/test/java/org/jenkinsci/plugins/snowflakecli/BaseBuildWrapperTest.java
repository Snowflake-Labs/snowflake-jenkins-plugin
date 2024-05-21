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