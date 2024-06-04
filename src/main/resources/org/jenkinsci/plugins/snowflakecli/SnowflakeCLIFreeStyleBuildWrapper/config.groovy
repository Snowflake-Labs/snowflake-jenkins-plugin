package org.jenkinsci.plugins.snowflakecli.SnowflakeBuildWrapper
f = namespace('/lib/form')


f.block() {
    f.div(style: "margin: 0px 0px") {
        f.table(style: "width: 100%") {
            f.entry(field: 'snowflakeInstallation', title: _('Select installer')) {
                f.select();
            }
            
            f.radioBlock(checked: descriptor.isInlineConfigChecked(instance), name: 'config', value: 'inline', title: 'Configuration Text') {
                f.entry(title: 'Snowflake CLI Text Configuration', field: 'inlineConfig', description: 'Inline configuration') {
                    f.textarea();
                }
            }
            f.radioBlock(checked: descriptor.isFileConfigChecked(instance), name: 'config', value: 'file', title: 'Configuration Path') {
                f.entry(title: 'Snowflake CLI File Configuration', field: 'fileConfig', description: 'Relative Path to the configuration file') {
                    f.textbox();
                }
            }
        }
    }
}