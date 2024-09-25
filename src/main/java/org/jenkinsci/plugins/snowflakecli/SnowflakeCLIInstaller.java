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

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolInstaller;
import hudson.tools.ToolInstallerDescriptor;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormValidation;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SnowflakeCLIInstaller extends ToolInstaller {
    private final String version;
    private final String pythonVersion;
    private static final String SNOW_VERSION = ".snow_version";
    private static final Logger LOGGER = Logger.getLogger(SnowflakeCLIInstaller.class.getName());
    
    
    @DataBoundConstructor
    public SnowflakeCLIInstaller(String label, String version, String pythonVersion){
        super(label);
        
        this.version = version;
        this.pythonVersion = pythonVersion;
    }

    public String getVersion() {
        return this.version;
    }
    
    public String getPythonVersion() {
        return this.pythonVersion;
    }
    
    protected boolean isUpToDate(FilePath expectedLocation, String version) throws IOException, InterruptedException {
        FilePath snowVersionFile = expectedLocation.child(SNOW_VERSION);
        return snowVersionFile.exists() && snowVersionFile.readToString().equals(version);
    }
    
    private String getInstallationScript() throws IOException, InterruptedException {
        return Utils.getClassResourceContent(this.getClass(), "InstallSnowflakeCLI.sh");
    }
    
    public FilePath performInstallation(ToolInstallation tool, Node node, TaskListener log) throws IOException, InterruptedException {
        FilePath dir = this.preferredLocation(tool, node);
        String venvFolderName = "snow_cli_executable_venv";
        FilePath script = null;
        FilePath venvDirectory = dir.child(venvFolderName);
        String version = getVersion();
        String pythonVersion = (this.getPythonVersion().equalsIgnoreCase("default") ? "" : this.getPythonVersion());
        if (version.equalsIgnoreCase(Utils.LATEST) ) {
            version = Utils.getLatestSnowflakeCliVersion();
        }
        if(isUpToDate(venvDirectory, version))
        {
            return venvDirectory;
        }
        
        try {
            script = dir.createTextTempFile("installSnowflake", ".sh", this.getInstallationScript());
            if(venvDirectory.child(venvFolderName).exists())
            {
                venvDirectory.child(venvFolderName).deleteRecursive();
            }
            
            ArgumentListBuilder args = new ArgumentListBuilder();
            args.add(Utils.getCommandCall(script));
            args.add(venvDirectory.getRemote());
            args.add(version);
            args.add(pythonVersion);
            int r = node.createLauncher(log).launch().cmds(args).stdout(log).pwd(dir).join();
            if (r != 0) {
                log.error(Messages.CommandReturnedStatus(r));
                throw new InterruptedException(Messages.CommandReturnedStatus(r));
            }
            
            venvDirectory.child(SNOW_VERSION).write(version, "UTF-8");
            
        } finally {
            if(script != null && script.exists()){
                script.delete();
            }
        }
        
        return venvDirectory;
    }
    

    @Extension
    public static class Descriptor<TInstallerClass extends SnowflakeCLIInstaller> extends ToolInstallerDescriptor<TInstallerClass> {
        public Descriptor() {
        }
        
        @NonNull
        public String getDisplayName() {
            return Messages.BuildWrapperName();
        }
        
        public FormValidation doCheckVersion(@QueryParameter String value){
            if(this.checkVersion(value))
            {
                return FormValidation.ok();
            }
            else{
                return FormValidation.error(Messages.ErrorNonValidVersion(value));
            }
        }
        
        public FormValidation doCheckPythonVersion(@QueryParameter String value){
            if(this.checkPythonVersion(value))
            {
                return FormValidation.ok();
            }
            else{
                return FormValidation.error(Messages.ErrorNonValidPythonVersion(value));
            }
        }
        
        private boolean checkVersion(String input){
            Pattern expr = Pattern.compile(Utils.getVersionPatternWithLatest(), Pattern.CASE_INSENSITIVE);
            Matcher matcher = expr.matcher(input);
            return matcher.matches();
        }
        
        private boolean checkPythonVersion(String input){
            String pythonCommandPattern = "^(?:[a-z0-9\\.]*)$";
            Pattern expr = Pattern.compile(pythonCommandPattern, Pattern.CASE_INSENSITIVE);
            Matcher matcher = expr.matcher(input);
            return matcher.matches();
        }
    }
}

