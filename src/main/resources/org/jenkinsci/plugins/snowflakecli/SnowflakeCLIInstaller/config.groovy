package org.jenkinsci.plugins.snowflakecli.SnowflakeBuildWrapper
f = namespace('/lib/form')

f.block() {
    f.div(style: "margin: 0px 0px") {
        f.table(style: "width: 100%") {
            f.entry(field: 'version', title: _('Install with pipx command'), description: 'Snowflake CLI version to be installed') {
                f.textbox();
            }
        }
    }
}