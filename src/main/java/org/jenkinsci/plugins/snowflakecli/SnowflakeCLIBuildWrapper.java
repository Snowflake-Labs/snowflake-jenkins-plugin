/*
 * Copyright 2024 Snowflake Inc. 
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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