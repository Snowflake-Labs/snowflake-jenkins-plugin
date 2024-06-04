package org.jenkinsci.plugins.snowflakecli;

import hudson.*;

import hudson.model.*;

import jenkins.tasks.SimpleBuildWrapper;

import java.io.*;

public abstract class SnowflakeCLIBuildWrapperBase extends SimpleBuildWrapper {
    
    @Override
    public boolean requiresWorkspace() {return true;}
    
    public String copyConfigFile(FilePath sourceFile, FilePath destinationFile) {
        return "mv \"" + sourceFile.getRemote() + "\" \"" + destinationFile.getRemote() + "\"\n" +
                "chmod 0600 \"" + destinationFile.getRemote() + "\"";
    }
    
    public void setUpConfigFile(FilePath workspace, FilePath snowflakeHome, final Launcher launcher, final TaskListener listener, EnvVars initialEnvironment) throws IOException, InterruptedException {
        FilePath oldConfigFile = this.getTemporalConfigurationFile(workspace, initialEnvironment, listener);
        FilePath configurationFile = new FilePath(snowflakeHome, "config.toml");
        FilePath script = snowflakeHome.createTextTempFile("setConfigFile", ".sh", this.copyConfigFile(oldConfigFile, configurationFile));
        
        String[] cmd = Utils.getCommandCall(script);
        int result = launcher.launch().pwd(snowflakeHome.getRemote()).cmds(cmd).stdout(listener).join();

        if (result != 0) {
            throw new InterruptedException(Messages.ErrorSettingSnowflakeConfigFile());
        }
    }
    
    @Override
    public void setUp(Context context, Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener, EnvVars initialEnvironment) throws IOException, InterruptedException {
        try {
            FilePath snowflakeHome = this.setUpSnowflakeHome(workspace, launcher, listener, initialEnvironment);
            context.env("SNOWFLAKE_HOME", snowflakeHome.getRemote());
            context.setDisposer(new SnowflakeDisposer(snowflakeHome.getRemote()));
            
        }catch (Exception ex) {
            listener.fatalError(exceptionToString(ex));
        }
    }
    
    public FilePath setUpSnowflakeHome(FilePath workspace, final Launcher launcher, final TaskListener listener, EnvVars initialEnvironment) throws IOException, InterruptedException {
        listener.getLogger().println("Creating temporal Snowflake home directory");
        FilePath snowflakeHome  = workspace.createTempDir(".snowflake", "home");
        this.setUpConfigFile(workspace, snowflakeHome, launcher, listener, initialEnvironment);
        return snowflakeHome;
    }
    
    protected abstract FilePath getTemporalConfigurationFile(FilePath workspace, EnvVars env, TaskListener listener) throws IOException, InterruptedException;
    
    protected String exceptionToString(Exception ex) {
        StringWriter writer = new StringWriter();
        ex.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}