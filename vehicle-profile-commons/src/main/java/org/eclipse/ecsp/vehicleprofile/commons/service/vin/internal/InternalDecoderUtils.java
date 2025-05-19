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

package org.eclipse.ecsp.vehicleprofile.commons.service.vin.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Internal decoder utility class.
 */
public class InternalDecoderUtils {
    private static final Map<String, Integer> ALPHABET_INDEX = new HashMap<>();
    public static final int VIN_DECODE_SUBSTRING_SEVENTEEN = 17;
    public static final int VIN_DECODE_SUBSTRING_THREE = 3;
    public static final int VIN_DECODE_SUBSTRING_TWO = 2;
    public static final int VIN_DECODE_SUBSTRING_FIVE = 5;
    public static final int VIN_DECODE_SUBSTRING_SIX = 6;
    public static final int VIN_DECODE_SUBSTRING_NINE = 9;
    public static final int VIN_DECODE_SUBSTRING_FOUR = 4;
    
    private static final String[] ALPHABET_CHARS = { "A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "N",
      "P", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0" };

    static {
        for (int i = 0; i < ALPHABET_CHARS.length; i++) {
            ALPHABET_INDEX.put(ALPHABET_CHARS[i], i);
        }
    }

    /**
     * InternalDecoderUtils.
     */
    private InternalDecoderUtils() {
    }

    /**
     * validateVin.
     */
    public static void validateVin(String vin) {
        if (!isValidSize(vin)) {
            throw new IllegalArgumentException("Invalid Vin");
        } else {
            assertValidCharacters(vin);
        }
    }

    /**
     * Validate vin length, it must be ==17 chars.
     *
     * @param vin value as String
     * @return boolean
     */
    private static boolean isValidSize(String vin) {
        return vin != null && vin.length() == VIN_DECODE_SUBSTRING_SEVENTEEN;
    }

    private static void assertValidCharacters(String vin) {
        for (int i = 0, len = vin.length(); i < len; i++) {
            String str = vin.substring(i, i + 1);
            if (ALPHABET_INDEX.get(str) == null) {
                throw new IllegalArgumentException("Invalid Vin");
            }
        }
    }

    public static String getManufacturerCode(String vin) {
        return vin.substring(0, VIN_DECODE_SUBSTRING_THREE);
    }

    public static String getCountryCode(String vin) {
        return vin.substring(0, VIN_DECODE_SUBSTRING_TWO);
    }

    /**
     * getModelCode.
     */
    public static String getModelCode(String vin) {
        if (vin.substring(0, VIN_DECODE_SUBSTRING_THREE).equalsIgnoreCase("mhb")) {
            return vin.substring(VIN_DECODE_SUBSTRING_THREE, VIN_DECODE_SUBSTRING_FIVE);
        } else if (vin.substring(0, VIN_DECODE_SUBSTRING_THREE).equalsIgnoreCase("mnt") 
                && Pattern.matches(vin.substring(VIN_DECODE_SUBSTRING_SIX, VIN_DECODE_SUBSTRING_NINE), "D23")
                && vin.substring(VIN_DECODE_SUBSTRING_THREE, VIN_DECODE_SUBSTRING_FOUR).equalsIgnoreCase("j")) {
            return "W" + vin.substring(VIN_DECODE_SUBSTRING_SIX, VIN_DECODE_SUBSTRING_NINE);
        }
        return vin.substring(VIN_DECODE_SUBSTRING_SIX, VIN_DECODE_SUBSTRING_NINE);
    }
}
