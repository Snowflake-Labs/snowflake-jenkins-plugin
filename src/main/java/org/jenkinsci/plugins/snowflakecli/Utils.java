/*
 * Copyright 2024 Snowflake Inc. 
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jenkinsci.plugins.snowflakecli;

import hudson.FilePath;
import jenkins.model.Jenkins;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static String[] getCommandCall(FilePath script) {
        return new String[]{"sh", "-e", script.getRemote()};
    }
    
    public static final String VERSION_PATTERN = "(?:[\\d\\.]+[\\w-_]*\\d*)";
    public static final String LATEST = "latest";
    
    
    public static String getVersionPatternWithLatest(){
        return "^(?:"+ Utils.VERSION_PATTERN+"|"+ LATEST +")$";
    }
    
    
    public static String getVersionPattern(){
        return "^" + Utils.VERSION_PATTERN + "$";
    }
    
    public static String getClassResourceContent(Class inputClass, String resourceName) throws IOException {
        String packageName = inputClass.getPackageName();
        String className = inputClass.getSimpleName();
        String scriptPath = packageName.replace(".", "/") + "/" + className + "/" + resourceName;
        byte[] encodedFile = Jenkins.getInstanceOrNull().pluginManager.uberClassLoader.getResourceAsStream(scriptPath).readAllBytes();
        return new String(encodedFile, "UTF-8");
    }
    
    public static String getLatestSnowflakeCliVersion() throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("https://pypi.org/pypi/snowflake-cli-labs/json");
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    throw new IOException(EntityUtils.toString(response.getEntity()));
                }
                String responseBody = EntityUtils.toString(response.getEntity());
                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONObject info = jsonResponse.getJSONObject("info");
                URL url = new URL(info.getString("release_url"));
                String path = url.getPath();
                String[] pathParts = path.split("/");
                String lastPart = pathParts[pathParts.length - 1];
                
                Pattern pattern = Pattern.compile(Utils.getVersionPattern(), Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(lastPart);
                if(!matcher.matches()) {
                    throw new IOException(Messages.FailedToRetrieveSnowflakeCLI());
                }
                return lastPart;
            }
        }
    }
}
