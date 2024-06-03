package org.jenkinsci.plugins.snowflakecli;

import hudson.*;

import hudson.model.Computer;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildWrapper;
import hudson.util.ListBoxModel;

import hudson.model.AbstractProject;

import hudson.tasks.BuildWrapperDescriptor;

import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.*;

import java.util.logging.Logger;

public class SnowflakeFreeStyleBuildWrapper extends SnowflakeBuildWrapperBase {
    private Configuration config;
    private String snowflakeInstallation;

    @DataBoundConstructor
    public SnowflakeFreeStyleBuildWrapper() {
    }

    @DataBoundSetter
    public void setConfig(Configuration config) {
        this.config = config;
    }
    
    @DataBoundSetter
    public void setSnowflakeInstallation(String snowflakeInstallation) {
        this.snowflakeInstallation = snowflakeInstallation;
    }
    
    public String getSnowflakeInstallation()
    {
        return snowflakeInstallation;
    }

    public String getInlineConfig() {
        if (config == null) {
            return null;
        }
        
        return this.config.getInlineConfig();
    }
    
    public String getFileConfig() {
        if (config == null) {
            return null;
        }
        
        return this.config.getFileConfig();
    }
    
    public Configuration.Mode getMode() {
        return this.config.getMode();
    }
    
    @Override
    public void setUp(Context context, Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener, EnvVars initialEnvironment) throws IOException, InterruptedException {
        SnowflakeInstallation installation = getInstallation();
        FilePath workspacePath = this.setupSnowflakeHome(workspace, launcher, listener, initialEnvironment);
        if (installation != null) {
            installation = installation.forNode(Computer.currentComputer().getNode(), listener).forEnvironment(initialEnvironment);
            EnvVars envVars = new EnvVars();
            installation.buildEnvVars(envVars);
            context.getEnv().putAll(envVars);
        }
        else
        {
            throw new AbortException(Messages.InstallationNotFound());
        }
        
        context.env("SNOWFLAKE_HOME", workspacePath.getRemote());
        context.setDisposer(new SnowflakeDisposer(workspacePath));
    }
    

    @Override
    public SnowflakeFreeStyleDescriptor getDescriptor() {
        return (SnowflakeFreeStyleDescriptor) super.getDescriptor();
    }
    
    public SnowflakeInstallation getInstallation() {
        for (SnowflakeInstallation installation : ((SnowflakeFreeStyleDescriptor) getDescriptor()).getInstallations()) {
            if (this.getSnowflakeInstallation() != null &&
                installation.getName().equals(this.getSnowflakeInstallation())) {
                return installation;
            }
        }
        return null;
    }
    
    @Override
    protected FilePath getTemporalConfigurationFile(FilePath workspace, EnvVars env, TaskListener listener) throws IOException, InterruptedException {
        FilePath configFile = null;
        switch (this.getMode()) {
            case INLINE:
                configFile = workspace.createTextTempFile("config", ".toml", getInlineConfig());
                if (configFile == null || !configFile.exists()) {
                    throw new IOException(Messages.ConfigurationNotCreated());
                }
                
                listener.getLogger().println("Creating temporal config file in " + configFile.getRemote());
                break;
            case FILE:
                if (!isNullOrEmpty(getFileConfig())) {
                    configFile = new FilePath(workspace, getFileConfig());
                    if (!configFile.exists()) {
                        throw new FileNotFoundException(Messages.ConfigurationPathNotFound(getFileConfig()));
                    }
                    
                    listener.getLogger().println("Setting config file to " + getFileConfig());
                }
                else
                {
                   throw new FileNotFoundException(Messages.EmptyConfigurationPath());
                }
                
                break;
            default:
                throw new IOException(Messages.InvalidConfigMode());
        }
        
        return configFile;
    }

    private boolean isNullOrEmpty(String value) {
        return (value == null || value.trim().isEmpty()) ? true : false;
    }


    @Extension
    public static final class SnowflakeFreeStyleDescriptor extends BuildWrapperDescriptor {
        @CopyOnWrite
        protected volatile SnowflakeInstallation[] installations = new SnowflakeInstallation[0];
        
        public SnowflakeInstallation[] getInstallations() {
            return this.installations;
        }

        public void setInstallations(SnowflakeInstallation[] installations) {
            this.installations = installations;
            save();
        }
        
        public String getDisplayName() {
            return Messages.BuildWrapperName();
        }
        
        public SnowflakeFreeStyleDescriptor() {
            super(SnowflakeFreeStyleBuildWrapper.class);
            load();
        }
        
        public boolean isInlineConfigChecked(SnowflakeFreeStyleBuildWrapper instance) {
            boolean result = true;
            if (instance != null)
                return (instance.getInlineConfig() != null);

            return result;
        }


        public boolean isFileConfigChecked(SnowflakeFreeStyleBuildWrapper instance) {
            boolean result = false;
            if (instance != null)
                return (instance.getFileConfig() != null);

            return result;
        }

        public ListBoxModel doFillSnowflakeInstallationItems() {
            ListBoxModel m = new ListBoxModel();
            for (SnowflakeInstallation inst : this.installations) {
                m.add(inst.getName());
            }
            return m;
        }
        

        public boolean isApplicable(AbstractProject<?, ?> project) {
            return true;
        }
    }
}