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

package org.eclipse.ecsp.vehicleprofile.commons.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * JsonUtils.
 */
public class JsonUtils {

    /**
     * JsonUtils.
     */
    private JsonUtils() {
    }

    /**
     * createObjectFromJson.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Object createObjectFromJson(String jsonStr, Class clazz, boolean skipUnkownProperties)
            throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        if (skipUnkownProperties) {
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        Object object = mapper.readValue(jsonStr, clazz);
        return object;
    }

    /**
     * createJsonFromObject.
     */
    public static String createJsonFromObject(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsString(object);
    }

    /**
     * getValue.
     */
    public static String getValue(String jsonStr, String fieldName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonStr);
        JsonNode node = root.findValue(fieldName);
        return node != null ? node.asText() : "";
    }

}