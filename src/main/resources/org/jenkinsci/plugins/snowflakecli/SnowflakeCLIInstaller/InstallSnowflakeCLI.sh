#!/bin/bash

pipx_command="pipx"
pip_command="pip"
python_command=""
installation_path="$1"
version="$2"

# Installs pipx from pip if it is not installed in the current agent.
if ! command -v pipx >/dev/null 2>&1
then
  
    if command -v python3 >/dev/null 2>&1
    then
        python_command="python3"
        pip_command="pip3"
    elif command -v python >/dev/null 2>&1
    then
        python_command="python"
        pip_command="pip"
    else
        exit 2
    fi
    
    if ! eval "$python_command -m $pip_command show pipx" > /dev/null 2>&1; then
        eval "$python_command -m $pip_command install pipx"
        pipx_command=" python -m pipx"
    fi

fi

if [ ! -z "$2" ]; then
    version="==$version"
fi

export PIPX_HOME="$installation_path"
export PIPX_BIN_DIR="$installation_path"
export PATH=$PIPX_BIN_DIR:$PATH
eval "$pipx_command install snowflake-cli-labs$version --force"