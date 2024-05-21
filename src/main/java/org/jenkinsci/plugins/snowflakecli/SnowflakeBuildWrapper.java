package org.jenkinsci.plugins.snowflakecli;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Extension;
import hudson.CopyOnWrite;

import hudson.util.ListBoxModel;
import hudson.util.FormValidation;
import hudson.util.ArgumentListBuilder;

import hudson.model.Result;
import hudson.model.Computer;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.model.EnvironmentContributingAction;

import hudson.model.Descriptor.FormException;

import hudson.tasks.Recorder;
import hudson.tasks.Publisher;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildWrapperDescriptor;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.DataBoundConstructor;

import net.sf.json.JSONObject;

import java.io.*;
import java.util.Map;

import java.util.logging.Logger;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class SnowflakeBuildWrapper extends BuildWrapper {
    private String snowflakeInstallation;
    private Configuration config;
    private FilePath configurationFile;
    
    private static final Logger LOGGER = Logger.getLogger(SnowflakeBuildWrapper.class.getName());


    @DataBoundConstructor
    public SnowflakeBuildWrapper() {
    }

    @DataBoundSetter
    public void setConfig(Configuration config) {
        this.config = config;
    }

    @DataBoundSetter
    public void setSnowflakeInstallation(String snowflakeInstallation) {
        this.snowflakeInstallation = snowflakeInstallation;
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
    

    public SnowflakeInstallation getInstallation() {
        for (SnowflakeInstallation installation : ((DescriptorImpl) getDescriptor()).getInstallations()) {
            if (snowflakeInstallation != null &&
                installation.getName().equals(snowflakeInstallation)) {
                return installation;
            }
        }
        return null;
    }


    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public Environment setUp(AbstractBuild build, final Launcher launcher, final BuildListener listener) throws IOException, InterruptedException {
        try {
            LOGGER.info("Getting Snowflake CLI installation");
            SnowflakeInstallation installation = getInstallation();
            FilePath workspacePath = this.setupSnowflakeHome(build, launcher, listener);
            if (installation != null) {
                EnvVars env = build.getEnvironment(listener);
                env.overrideAll(build.getBuildVariables());
                installation = installation.forNode(Computer.currentComputer().getNode(), listener).forEnvironment(env);
            }

            final SnowflakeInstallation install = installation;
            final FilePath workspace = workspacePath;
            return new Environment() {
                @Override
                public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
                    deleteTemporaryFiles();
                    return true;
                }
                @Override
                public void buildEnvVars(Map<String, String> env) {
                    if (install != null) {
                        EnvVars envVars = new EnvVars();
                        install.buildEnvVars(envVars);
                        env.putAll(envVars);
                        env.put("SNOWFLAKE_HOME", workspace.getRemote());
                    }
                }
            };
            
        }catch (Exception ex) {
            LOGGER.severe(exceptionToString(ex));
            listener.fatalError(exceptionToString(ex));
            return null;
        }

    }
    
    private void deleteTemporaryFiles() throws IOException, InterruptedException {
        if (configurationFile != null && configurationFile.exists())
            configurationFile.delete();
    }
  
    public String copyConfigFile(FilePath sourceFile, FilePath destinationFile) {
        return "mv \"" + sourceFile.getRemote() + "\" \"" + destinationFile.getRemote() + "\"\n" +
                "chmod 0600 \"" + destinationFile.getRemote() + "\"";
    }
    
    public FilePath setupSnowflakeHome(AbstractBuild build, final Launcher launcher, final BuildListener listener) throws Exception {
        LOGGER.info("Setting Snowflake Configuration File");
        EnvVars env = build.getEnvironment(listener);
        FilePath oldConfigFile = this.getConfigurationFile(build, env);
        FilePath workspacePath  = build.getWorkspace().createTempDir("snowflake", "home");
        this.configurationFile = new FilePath(workspacePath, "config.toml");
        FilePath script = build.getWorkspace().createTextTempFile("setConfigFile", ".sh", this.copyConfigFile(oldConfigFile, this.configurationFile));
        
        String[] cmd = Utils.getCommandCall(script);
        int result = launcher.launch().pwd(workspacePath.getRemote()).cmds(cmd).stdout(listener).join();

        if (result != 0) {
            throw new Exception(Messages.ErrorSettingSnowflakeConfigFile());
        }
        
        return workspacePath;
    }
    
    private String[] getCommandCall(FilePath script) {
        return new String[]{"sh", "-e", script.getRemote()};
    }
    
    private FilePath getConfigurationFile(AbstractBuild build, EnvVars env) throws FileNotFoundException, Exception {
        FilePath configFile = null;
        switch (this.getMode()) {
            case INLINE:
                configFile = build.getWorkspace().createTextTempFile("config", ".toml", evalEnvVars(getInlineConfig(), env));
                if (configFile == null || !configFile.exists()) {
                    throw new FileNotFoundException(Messages.ConfigurationNotCreated());
                }
                break;
            case FILE:
                if (!isNullOrEmpty(getFileConfig())) {
                    configFile = new FilePath(build.getWorkspace(), getFileConfig());
                    if (!configFile.exists()) {
                        throw new FileNotFoundException(Messages.ConfigurationPathNotFound(configFile));
                    }
                }
                else
                {
                   throw new FileNotFoundException(Messages.EmptyConfigurationPath());
                }
                
                break;
            default:
                throw new Exception(Messages.InvalidConfigMode());
        }
        
        return configFile;
    }


    private String evalEnvVars(String input, EnvVars env) throws Exception {
        String envPattern = "\\$([A-Z-a-z_0-9]+)";

        Pattern expr = Pattern.compile(envPattern);

        Matcher matcher = expr.matcher(input);

        String output = input;

        while (matcher.find()) {
            String envFound = env.get(matcher.group(1));

            if (envFound != null) {
               output = output.replace("$"+matcher.group(1), envFound);
            }
        }

        return output;
    }

    private boolean isNullOrEmpty(String value) {
        return (value == null || value.trim().isEmpty()) ? true : false;
    }


    private String exceptionToString(Exception ex) {
        StringWriter writer = new StringWriter();
        ex.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }


    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {

        @CopyOnWrite
        private volatile SnowflakeInstallation[] installations = new SnowflakeInstallation[0];


        public DescriptorImpl() {
            super(SnowflakeBuildWrapper.class);
            load();
        }
        
        public boolean isInlineConfigChecked(SnowflakeBuildWrapper instance) {
            boolean result = true;
            if (instance != null)
                return (instance.getInlineConfig() != null);

            return result;
        }


        public boolean isFileConfigChecked(SnowflakeBuildWrapper instance) {
            boolean result = false;
            if (instance != null)
                return (instance.getFileConfig() != null);

            return result;
        }


        public SnowflakeInstallation[] getInstallations() {
            return this.installations;
        }


        public void setInstallations(SnowflakeInstallation[] installations) {
            this.installations = installations;
            save();
        }
        
        public ListBoxModel doFillSnowflakeInstallationItems() {
            ListBoxModel m = new ListBoxModel();
            for (SnowflakeInstallation inst : installations) {
                m.add(inst.getName());
            }
            return m;
        }
        

        public boolean isApplicable(AbstractProject<?, ?> project) {
            return true;
        }


        public String getDisplayName() {
            return Messages.BuildWrapperName();
        }
    }
}