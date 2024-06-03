package org.jenkinsci.plugins.snowflakecli;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeSpecific;
import hudson.tools.*;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class SnowflakeInstallation extends ToolInstallation implements EnvironmentSpecific<SnowflakeInstallation>,
        NodeSpecific<SnowflakeInstallation> {
    private static final Logger LOGGER = Logger.getLogger(SnowflakeCLIInstaller.class.getName());
    

    @DataBoundConstructor
    public SnowflakeInstallation(String name, String home, List<? extends ToolProperty<?>> properties) {
        super(name, home, properties);
    }

    @Override
    public void buildEnvVars(EnvVars env) {
        String root = getHome();
        if (root != null) {
            env.put("PATH+SNOWFLAKECLI_BIN", new File(root).toString());
        }
    }

    public SnowflakeInstallation forEnvironment(EnvVars environment) {
        return new SnowflakeInstallation(getName(), environment.expand(getHome()), getProperties().toList());
    }

    public SnowflakeInstallation forNode(Node node, TaskListener log) throws IOException, InterruptedException {
        return new SnowflakeInstallation(getName(), translateFor(node, log), getProperties().toList());
    }

    @Extension
    @Symbol("snowflakecli")
    public static class DescriptorImpl extends ToolDescriptor<SnowflakeInstallation> {

        @Override
        public String getDisplayName() {
            return "Snowflake CLI";
        }

        @Override
        public List<? extends ToolInstaller> getDefaultInstallers() {
            return Collections.singletonList(new SnowflakeCLIInstaller(null,""));
        }

        @Override
        public SnowflakeInstallation[] getInstallations() {
            return Jenkins.getActiveInstance()
                    .getDescriptorByType(SnowflakeCLIFreeStyleBuildWrapper.SnowflakeFreeStyleDescriptor.class)
                    .getInstallations();
        }

        @Override
        public void setInstallations(SnowflakeInstallation... installations) {
            Jenkins.getActiveInstance()
                    .getDescriptorByType(SnowflakeCLIFreeStyleBuildWrapper.SnowflakeFreeStyleDescriptor.class)
                    .setInstallations(installations);
        }

    }

}