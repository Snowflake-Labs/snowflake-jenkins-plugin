package org.jenkinsci.plugins.snowflakecli;

import hudson.*;

import hudson.model.*;

import hudson.tasks.BuildWrapperDescriptor;

import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.*;
import java.util.logging.Logger;

public class SnowflakeBuildWrapper extends SnowflakeBuildWrapperBase {
    private String configFilePath;
    
    private String snowflakeInstallation;
    private static final Logger LOGGER = Logger.getLogger(SnowflakeBuildWrapper.class.getName());
    
        
    @DataBoundConstructor
    public SnowflakeBuildWrapper() {
    }

    @DataBoundSetter
    public void setConfigFilePath(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    public String getSnowflakeInstallation()
    {
        return snowflakeInstallation;
    }
    
    @DataBoundSetter
    public void setSnowflakeInstallation(String snowflakeInstallation) {
        this.snowflakeInstallation = snowflakeInstallation;
    }

    
    public String getConfigFilePath() {
        return this.configFilePath;
    }
    
    @Override
    public void setUp(Context context, Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener, EnvVars initialEnvironment) throws IOException, InterruptedException {
        try {
            FilePath workspacePath = this.setupSnowflakeHome(workspace, launcher, listener, initialEnvironment);
            context.env("SNOWFLAKE_HOME", workspacePath.getRemote());
            context.setDisposer(new SnowflakeDisposer(workspacePath));
            
        }catch (Exception ex) {
            LOGGER.severe(exceptionToString(ex));
            listener.fatalError(exceptionToString(ex));
        }
    }
    
    @Override
    public SnowflakeDescriptor getDescriptor() {
        return (SnowflakeDescriptor) super.getDescriptor();
    }
    
    @Override
    protected FilePath getTemporalConfigurationFile(FilePath workspace, EnvVars env, TaskListener listener) throws IOException, InterruptedException {
        FilePath configFile = null;
        if (new File(getConfigFilePath()).exists()) {
            configFile = new FilePath(workspace, getConfigFilePath());
        }
        else
        {
            throw new IOException(Messages.ConfigurationPathNotFound(getConfigFilePath()));
        }
        
        return configFile;
    }
    
    @Extension
    public static final class SnowflakeDescriptor extends BuildWrapperDescriptor {
        
        public String getDisplayName() {
            return Messages.BuildWrapperName();
        }
        public SnowflakeDescriptor() {
            super(SnowflakeBuildWrapper.class);
            load();
        }

        public boolean isApplicable(AbstractProject<?, ?> project) {
            return false;
        }
    }
}