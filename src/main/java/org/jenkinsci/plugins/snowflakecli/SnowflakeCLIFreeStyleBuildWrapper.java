package org.jenkinsci.plugins.snowflakecli;

import hudson.*;

import hudson.model.*;
import hudson.util.ListBoxModel;

import hudson.tasks.BuildWrapperDescriptor;

import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.*;

public class SnowflakeCLIFreeStyleBuildWrapper extends SnowflakeCLIBuildWrapperBase {
    private Configuration config;
    private String snowflakeInstallation;

    @DataBoundConstructor
    public SnowflakeCLIFreeStyleBuildWrapper() {
    }

    @DataBoundSetter
    public void setConfig(final Configuration config) {
        this.config = config;
    }
    
    @DataBoundSetter
    public void setSnowflakeInstallation(final String snowflakeInstallation) {
        this.snowflakeInstallation = snowflakeInstallation;
    }
    
    public String getSnowflakeInstallation() {
        return snowflakeInstallation;
    }
    
    public Configuration getConfig() {
        return this.config;
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
        try {
            this.setUpSnowflakeInstallation(context, listener, initialEnvironment);
        }
        catch (Exception ex) {
            build.setResult(Result.FAILURE);
            listener.fatalError(exceptionToString(ex));
        }
        
        super.setUp(context, build, workspace, launcher, listener, initialEnvironment);
    }
    
    public void setUpSnowflakeInstallation(Context context, TaskListener listener, EnvVars initialEnvironment) throws IOException, InterruptedException {
        SnowflakeInstallation installation = getInstallation();
        if (installation != null) {
            installation = installation.forNode(Computer.currentComputer().getNode(), listener).forEnvironment(initialEnvironment);
            EnvVars envVars = new EnvVars();
            installation.buildEnvVars(envVars);
            context.getEnv().putAll(envVars);
        }
        else {
            throw new InterruptedException(Messages.InstallationNotFound());
        }
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
                    throw new InterruptedException(Messages.ConfigurationNotCreated());
                }
                
                listener.getLogger().println("Creating temporal config file in " + configFile.getRemote());
                break;
            case FILE:
                if (!isNullOrEmpty(getFileConfig())) {
                    configFile = new FilePath(workspace, getFileConfig());
                    if (!configFile.exists()) {
                        throw new IOException(Messages.ConfigurationPathNotFound(getFileConfig()));
                    }
                    
                    listener.getLogger().println("Setting config file to " + getFileConfig());
                }
                else
                {
                   throw new IOException(Messages.EmptyConfigurationPath());
                }
                
                break;
            default:
                throw new InterruptedException(Messages.InvalidConfigMode());
        }
        
        return configFile;
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
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
            super(SnowflakeCLIFreeStyleBuildWrapper.class);
            load();
        }
        
        public boolean isInlineConfigChecked(SnowflakeCLIFreeStyleBuildWrapper instance) {
            boolean result = true;
            if (instance != null)
                return (instance.getInlineConfig() != null);

            return result;
        }


        public boolean isFileConfigChecked(SnowflakeCLIFreeStyleBuildWrapper instance) {
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