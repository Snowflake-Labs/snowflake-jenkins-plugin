# Snowflake CLI Plugin

## About this Plugin

The Snowflake CLI Plugin for [Jenkins](https://jenkins.io) provides an installer for [Snowflake CLI](https://docs.snowflake.com/en/developer-guide/snowflake-cli-v2/index) and a build wrapper for setting the Snowflake configuration file.

## Functionality

During a build, this plugin can:
- Install a specific version of Snowflake CLI on the agent.
- Add the executable path to the agent's PATH variable.
- Create a temporary directory for storing the configuration file and scripts.
- Export the `SNOWFLAKE_HOME` environment variable pointing to the temporary directory.

### Global Configuration

1. In the System Configuration (_Manage Jenkins > System Configuration > Tools_), find the "Snowflake CLI Installations" section, and click "Add Snowflake CLI."
2. Enter a label, e.g., "latest" — this label is used in Pipelines or displayed during Freestyle job configuration.
3. Enter the desired version to be installed or specify another installation method.

### Per-Job Configuration

#### Declarative Pipeline

Use the [`tools` directive](https://www.jenkins.io/doc/book/pipeline/syntax/#tools) within any `pipeline` or `stage`. For example:

```groovy
pipeline {
  // Run on an agent where we want to use Snowflake CLI
  agent any

  // Ensure the desired Snowflake CLI version is installed for all stages,
  // using the name defined in the Global Tool Configuration
  tools { snowflakecli 'latest' }

  stages {
    stage('Build') {
      steps {
        script {
          // Use a wrap directive, set $class as SnowflakeCLIBuildWrapper and add the parameter for the path to the configuration file in your repository.
          // This wrapper copies the information of the input file and stores it to a temporal config.toml file.
          wrap([$class: 'SnowflakeCLIBuildWrapper', configFilePath: 'config.toml']) {
              sh 'snow connection list'
          }
        }
      }
    }
  }
```

#### Scripted Pipeline

```groovy
node {
    // Ensure the desired Snowflake CLI version is installed for all stages,
    // using the name defined in the Global Tool Configuration
    def root = tool type: 'snowflakecli', name: '2.4.0'

    // Manually set the PATH variable to the snow executable.
    withEnv(["PATH+SNOWFLAKECLI=${root}"]) {
       // Use a wrap directive, set $class as SnowflakeCLIBuildWrapper and add the parameter for the path to the configuration file in your repository.
       // This wrapper copies the information of the input file and stores it to a temporal config.toml file.
       wrap([$class: 'SnowflakeCLIBuildWrapper', configFilePath: 'config.toml']) {
          sh 'snow --version'
          sh 'snow connection list'
       }
    }
}
```

#### Freestyle

1. In a job's configuration, find the "Build environment" section.
2. Select the "Install Snowflake CLI" checkbox.
3. Select the name of a Snowflake installation from the drop-down.
4. Choose inline or file mode:
   1. For file mode, enter the path to the `config.toml`.
   2. For inline mode, enter the `config.toml` text.

## License

Licensed under the Apache License. See [LICENSE](LICENSE.md) for details.
