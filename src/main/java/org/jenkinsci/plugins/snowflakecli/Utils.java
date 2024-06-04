package org.jenkinsci.plugins.snowflakecli;

import hudson.FilePath;
import jenkins.model.Jenkins;

import java.io.IOException;

public class Utils {
    public static String[] getCommandCall(FilePath script) {
        return new String[]{"sh", "-e", script.getRemote()};
    }
    
    public static String getClassResourceContent(Class inputClass, String resourceName) throws IOException {
        String packageName = inputClass.getPackageName();
        String className = inputClass.getSimpleName();
        String scriptPath = packageName.replace(".", "/") + "/" + className + resourceName;
        byte[] encodedFile = Jenkins.getInstanceOrNull().pluginManager.uberClassLoader.getResourceAsStream(scriptPath).readAllBytes();
        return new String(encodedFile, "UTF-8");
    }
}
