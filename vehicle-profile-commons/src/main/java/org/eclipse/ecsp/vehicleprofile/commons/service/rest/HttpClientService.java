/*
 *  *******************************************************************************
 *  Copyright (c) 2023-24 Harman International
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  *******************************************************************************
 */

package org.eclipse.ecsp.vehicleprofile.commons.service.rest;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * HttpClientService.
 */
@Service
public class HttpClientService {

    public static final int HTTP_RETRY_HANDLER_COUNT = 3;

    /**
     * executeGETMethod.
     */
    public String executeGetMethod(String url) throws IOException {
        String responseJson;

        // Create an instance of CloseableHttpClient
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setRetryHandler(new DefaultHttpRequestRetryHandler(3, false)) // 3 retries
                .build()) {
            // Create an HTTP GET request
            HttpGet httpGet = new HttpGet(url);

            // Execute the request
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                // Check the status code
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    throw new IOException("Method failed: " + response.getStatusLine());
                }

                // Get the response entity
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    // Convert the response entity to a String
                    responseJson = EntityUtils.toString(entity);
                } else {
                    throw new IOException("Response entity is null");
                }
            }
        }
        return responseJson;
    }
}
