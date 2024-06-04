package org.jenkinsci.plugins.snowflakecli;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.tasks.SimpleBuildWrapper;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class SnowflakeDisposer extends SimpleBuildWrapper.Disposer implements Serializable {
    private static final long serialVersionUID = 1L;

    private String snowflakeHome;

    public SnowflakeDisposer(String snowflakeHome) {
        this.snowflakeHome = snowflakeHome;
    }

    public void deleteTemporaryFiles() throws IOException, InterruptedException {
        FilePath filePath = new FilePath(new File(this.snowflakeHome));
        if(filePath != null && filePath.exists()){
            filePath.deleteRecursive();
        }
    }

    @Override
    public void tearDown(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
        listener.getLogger().println("Deleting temporal Snowflake home directory");
        deleteTemporaryFiles();
    }
}