package org.jenkinsci.plugins.snowflakecli;

import hudson.FilePath;

public class Utils {
    public static String[] getCommandCall(FilePath script) {
        return new String[]{"sh", "-e", script.getRemote()};
    }
}
