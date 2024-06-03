package org.jenkinsci.plugins.snowflakecli;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;


public class Configuration {
    private final String value;
    private final String fileConfig;
    private final String inlineConfig;
    private Mode mode;

    public enum Mode {
        INLINE, FILE, INVALID
    }

    @DataBoundConstructor
    public Configuration(String value, String inlineConfig, String fileConfig) {
        this.value = value;
        this.fileConfig = fileConfig;
        this.inlineConfig = inlineConfig;
        this.mode = getMode(value);
    }
    
    public Mode getMode() {
        if(this.mode == null) {
             this.mode = getMode(value);
        }
        
        return this.mode;
    }
    
    public String getInlineConfig() {
        return this.inlineConfig;
    }


    
    public String getFileConfig() {
        return this.fileConfig;
    }

    
    public String getValue() {
        return this.value;
    } 
    
    private Mode getMode(String mode)
    {
        mode = mode.trim().toUpperCase();
        try {
            return Mode.valueOf(mode);
        }
        catch (IllegalArgumentException e) {
            return Mode.INVALID;
        }
    }
}