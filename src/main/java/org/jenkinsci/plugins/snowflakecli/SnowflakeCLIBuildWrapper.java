package org.jenkinsci.plugins.snowflakecli;

import hudson.*;

import hudson.model.*;

import hudson.tasks.BuildWrapperDescriptor;

import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.*;

public class SnowflakeCLIBuildWrapper extends SnowflakeCLIBuildWrapperBase {
    private String configFilePath;
    
    private String snowflakeInstallation;
        
    @DataBoundConstructor
    public SnowflakeCLIBuildWrapper() {
    }

    @DataBoundSetter
    public void setConfigFilePath(final String configFilePath) {
        this.configFilePath = configFilePath;
    }

    public String getSnowflakeInstallation()
    {
        return snowflakeInstallation;
    }
    
    @DataBoundSetter
    public void setSnowflakeInstallation(final String snowflakeInstallation) {
        this.snowflakeInstallation = snowflakeInstallation;
    }
    
    public String getConfigFilePath() {
        return this.configFilePath;
    }
    
    @Override
    public SnowflakeDescriptor getDescriptor() {
        return (SnowflakeDescriptor) super.getDescriptor();
    }
    
    @Override
    protected FilePath getTemporalConfigurationFile(FilePath workspace, EnvVars env, TaskListener listener) throws IOException, InterruptedException {
        FilePath configFile = new FilePath(workspace, getConfigFilePath());
        if (configFile.exists()) {
            listener.getLogger().println("Setting config file to " + getConfigFilePath());
            return configFile;
        }
        else
        {
            throw new IOException(Messages.ConfigurationPathNotFound(getConfigFilePath()));
        }
    }
    
    @Extension
    public static final class SnowflakeDescriptor extends BuildWrapperDescriptor {
        
        public String getDisplayName() {
            return Messages.BuildWrapperName();
        }
        public SnowflakeDescriptor() {
            super(SnowflakeCLIBuildWrapper.class);
            load();
        }

        public boolean isApplicable(AbstractProject<?, ?> project) {
            return false;
        }
    }
}