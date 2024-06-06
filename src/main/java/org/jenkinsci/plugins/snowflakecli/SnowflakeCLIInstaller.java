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
    private static final String SNOW_VERSION = ".snow_version";
    private static final Logger LOGGER = Logger.getLogger(SnowflakeCLIInstaller.class.getName());
    
    
    @DataBoundConstructor
    public SnowflakeCLIInstaller(String label, String version) {
        super(label);
        this.version = version;
    }

    public String getVersion() {
        return this.version;
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
        FilePath script = null;
        FilePath binDirectory = dir.child("snow_cli_executable_bin");
        if(isUpToDate(binDirectory, this.version))
        {
            return binDirectory;
        }
        
        try {
            script = dir.createTextTempFile("installSnowflake", ".sh", this.getInstallationScript());
            if(binDirectory.child("snow_cli_executable_bin").exists())
            {
                binDirectory.child("snow_cli_executable_bin").deleteRecursive();
            }
            
            ArgumentListBuilder args = new ArgumentListBuilder();
            args.add(Utils.getCommandCall(script));
            args.add(binDirectory.getRemote());
            args.add(this.version);
            int r = node.createLauncher(log).launch().cmds(args).stdout(log).pwd(dir).join();
            if (r != 0) {
                if(r == 2)
                {
                    log.error(Messages.PythonRequired());
                    throw new InterruptedException(Messages.PythonRequired());
                }
                else {
                    log.error(Messages.CommandReturnedStatus(r));
                    throw new InterruptedException(Messages.CommandReturnedStatus(r));
                }
            }
            
            binDirectory.child(SNOW_VERSION).write(this.version, "UTF-8");
            
        } finally {
            if(script != null && script.exists()){
                script.delete();
            }
        }
        
        return binDirectory;
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
        
        private boolean checkVersion(String input){
            String pattern = "^(?:[\\d\\.]+[\\w-_]*\\d*)$";
            Pattern expr = Pattern.compile(pattern);
            Matcher matcher = expr.matcher(input);
            return matcher.matches();
        }
    }
}

