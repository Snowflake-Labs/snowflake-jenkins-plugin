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
            build.setResult(Result.FAILURE);
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