package org.jenkinsci.plugins.snowflakecli;

import hudson.*;

import hudson.model.*;

import jenkins.tasks.SimpleBuildWrapper;

import java.io.*;

public abstract class SnowflakeCLIBuildWrapperBase extends SimpleBuildWrapper {
    
    public String copyConfigFile(FilePath sourceFile, FilePath destinationFile) {
        return "mv \"" + sourceFile.getRemote() + "\" \"" + destinationFile.getRemote() + "\"\n" +
                "chmod 0600 \"" + destinationFile.getRemote() + "\"";
    }
    
    public FilePath setupSnowflakeHome(FilePath workspace, final Launcher launcher, final TaskListener listener, EnvVars initialEnvironment) throws IOException, InterruptedException {
        listener.getLogger().println("Creating temporal Snowflake home directory");
        FilePath workspacePath  = workspace.createTempDir(".snowflake", "home");
        FilePath oldConfigFile = this.getTemporalConfigurationFile(workspace, initialEnvironment, listener);
        FilePath configurationFile = new FilePath(workspacePath, "config.toml");
        FilePath script = workspacePath.createTextTempFile("setConfigFile", ".sh", this.copyConfigFile(oldConfigFile, configurationFile));
        
        String[] cmd = Utils.getCommandCall(script);
        int result = launcher.launch().pwd(workspacePath.getRemote()).cmds(cmd).stdout(listener).join();

        if (result != 0) {
            throw new IOException(Messages.ErrorSettingSnowflakeConfigFile());
        }
        
        return workspacePath;
    }
    
    protected abstract FilePath getTemporalConfigurationFile(FilePath workspace, EnvVars env, TaskListener listener) throws IOException, InterruptedException;
    
    protected String exceptionToString(Exception ex) {
        StringWriter writer = new StringWriter();
        ex.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
    
    public class SnowflakeDisposer extends Disposer {
        private FilePath snowflakeHome;
    
        public SnowflakeDisposer(FilePath snowflakeHome) {
            this.snowflakeHome = snowflakeHome;
        }
        
        public void deleteTemporaryFiles() throws IOException, InterruptedException {
            if(snowflakeHome != null && snowflakeHome.exists()){
                snowflakeHome.deleteRecursive();
            }
        }
        
        @Override
        public void tearDown(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
            this.tearDown(build, listener);
        }
        
        @Override
        public void tearDown(Run<?, ?> build, TaskListener listener) throws IOException, InterruptedException {
            listener.getLogger().println("Deleting temporal Snowflake home directory");
            deleteTemporaryFiles();
        }
    }
}