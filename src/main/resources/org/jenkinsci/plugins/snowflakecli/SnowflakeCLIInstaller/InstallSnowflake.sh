#!/bin/bash

pipx_command="pipx"
installation_path="$1"
version="$2"

# Installs pipx from pip if it is not installed in the current agent.
if ! command -v pipx &> /dev/null
then
    if ! command -v python3 &> /dev/null
    then
        exit 1000
    fi
    if ! python3 -m pip show pipx > /dev/null 2>&1; then
        python3 -m pip install pipx
    fi
    
    pipx_command=" python3 -m pipx"
fi

if [ ! -z "$2" ]; then
    version="==$version"
fi

export PIPX_HOME="$installation_path"
export PIPX_BIN_DIR="$installation_path"
export PATH=$PIPX_BIN_DIR:$PATH
eval "$pipx_command install snowflake-cli-labs$version --force"