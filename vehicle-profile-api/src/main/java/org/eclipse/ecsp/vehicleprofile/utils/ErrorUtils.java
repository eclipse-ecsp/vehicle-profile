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

package org.eclipse.ecsp.vehicleprofile.utils;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * ErrorUtils class.
 */
public final class ErrorUtils {

    /**
     * Forbid instantiation.
     */
    private ErrorUtils() {
    }

    public static final int ERROR_MESSAGE_INDEX = 2;
    public static final int ERROR_MESSAGE_SIZE = 150;
    
    /**
     * buildError.
     */
    public static String buildError(String generalErrorMessage, Exception e, Map<Object, Object> requestInput) {
        StringBuilder errorSb = new StringBuilder(ERROR_MESSAGE_SIZE);
        errorSb.append("\n").append(generalErrorMessage).append("\n");
        if (!requestInput.isEmpty()) {
            errorSb.append("Request input:").append("\n");
            for (Map.Entry<Object, Object> m : requestInput.entrySet()) {
                Object key = m.getKey();
                Object value = m.getValue();
                errorSb.append("  ").append(key).append(":").append(value).append("\n");
            }
        }
        errorSb.append("Error Message: ").append(e.getMessage()).append("\n");
        errorSb.append("RootCause Message: ").append(ExceptionUtils.getRootCauseMessage(e)).append("\n");
        errorSb.append("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
                .append("\n");
        errorSb.append("Error StackTrace:").append("\n");
        errorSb.append("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
                .append("\n");
        errorSb.append(ExceptionUtils.getStackTrace(e)).append("\n");
        return errorSb.toString();
    }

    /**
     * extractErrorMessage.
     */
    public static String extractErrorMessage(String generalErrorMessage) {
        if (!StringUtils.isEmpty(generalErrorMessage) && generalErrorMessage.contains("[{")) {
            generalErrorMessage = generalErrorMessage
                    .substring(generalErrorMessage.indexOf("\":") + ERROR_MESSAGE_INDEX,
                            generalErrorMessage.indexOf("}]")).trim()
                    .replace("\"", "").replace("\\", "");
        }
        return generalErrorMessage;

    }
}
