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

import org.apache.commons.lang3.StringUtils;

/**
 * CommonUtils.
 */
public class CommonUtils {

    /**
     * The constant MASKED_VALUE_LENGTH.
     */
    public static final int MASKED_VALUE_LENGTH = 4;
    
    private CommonUtils() {}

    /**
     * Mask content.
     *
     * @param unmaskedValue the unmasked value
     * @return the string
     */
    public static String maskContent(String unmaskedValue) {
        return (StringUtils.isNotBlank(unmaskedValue) ? "****" 
             + (unmaskedValue.length() > MASKED_VALUE_LENGTH ? unmaskedValue.substring(MASKED_VALUE_LENGTH) : "") : "");
    }

}
