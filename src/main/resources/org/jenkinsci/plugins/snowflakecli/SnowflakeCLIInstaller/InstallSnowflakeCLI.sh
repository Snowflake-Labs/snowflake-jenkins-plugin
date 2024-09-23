#!/bin/bash

#
# Copyright 2024 Snowflake Inc. 
# SPDX-License-Identifier: Apache-2.0
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

venv_folder_name="$1"
version="$2"
python_version="$3"
python_command="python$3"


if [ ! -z "$2" ]; then
    version="==$version"
fi


eval "$python_command -m venv $venv_folder_name"
{ 
source "$venv_folder_name"/bin/activate
} || { 
. "$venv_folder_name"/bin/activate
}

pip install pipx --force
export PIPX_HOME="$venv_folder_name"
export PIPX_BIN_DIR="$venv_folder_name"
export PATH=$PIPX_BIN_DIR:$PATH
eval "$python_command -m pipx install snowflake-cli-labs$version --force"