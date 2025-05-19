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

import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.commons.dto.vin.PositionMatcherResult;
import org.eclipse.ecsp.vehicleprofile.commons.dto.vin.VinDecodeResult;
import org.eclipse.ecsp.vehicleprofile.commons.exception.VinDecodeException;
import org.eclipse.ecsp.vehicleprofile.commons.service.vin.AbstractPositionMatcherVinDecoder;
import org.eclipse.ecsp.vehicleprofile.commons.utils.JsonUtils;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Position based char matcher vin decoder.
 */
@Service
public class PositionMatcherVinDecoder extends AbstractPositionMatcherVinDecoder {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(PositionMatcherVinDecoder.class);

    public static final int VIN_DECODE_SUBSTRING_THREE = 3;
    public static final int VIN_DECODE_SUBSTRING_EIGHT = 8;
    public static final int VIN_DECODE_SUBSTRING_SIX = 6;
    public static final int VIN_DECODE_SUBSTRING_NINE = 9;
    public static final int VIN_DECODE_SUBSTRING_FOUR = 4;
    
    /**
     * decode.
     * 
     */
    @Override
    public String decode(String vinNumber) throws VinDecodeException {
        LOGGER.info("decode - START vin: {}", CommonUtils.maskContent(vinNumber));
        if (StringUtils.isEmpty(vinNumber)) {
            throw new VinDecodeException("vinNumber either null or empty");
        } else {
            try {
                String make = getProductLine(vinNumber);
                String model = getModel(vinNumber);
                String year = getYear(vinNumber);
                VinDecodeResult vinDecodeResult = new PositionMatcherResult(make, model, year);
                String decodedJson = JsonUtils.createJsonFromObject(vinDecodeResult);
                return decodedJson;
            } catch (Exception e) {
                throw new VinDecodeException(
                        "Error has occurred while decoding VIN: " + CommonUtils.maskContent(vinNumber), e);
            }
        }
    }

    private String getProductLine(String vinNumber) {
        String vinCodeThree = vinNumber.substring(0, VIN_DECODE_SUBSTRING_THREE);
        char vinCodeCharFour = vinNumber.charAt(VIN_DECODE_SUBSTRING_THREE);
        char vinYearCharFive = vinNumber.charAt(VIN_DECODE_SUBSTRING_FOUR);
        if (vinCodeThree.equals("56K")) {
            return PRODUCT_INDIAN;
        } else {
            if (vinCodeCharFour == 'V') {
                return PRODUCT_MAP_CHART_VAL.get(String.valueOf(vinYearCharFive)) != null
                        ? PRODUCT_MAP_CHART_VAL.get(String.valueOf(vinYearCharFive))
                        : PRODUCT_RZR;
            } else if (vinCodeCharFour == 'S') {
                return PRODUCT_MAP_CHART_VAL.get(String.valueOf(vinYearCharFive)) != null
                        ? PRODUCT_MAP_CHART_VAL.get(String.valueOf(vinYearCharFive)).equals("V") ? PRODUCT_SCRAMBLER
                                : PRODUCT_SPORTSMAN
                        : PRODUCT_SPORTSMAN;
            } else if (vinCodeCharFour == 'D') {
                return PRODUCT_ACE;
            } else if (vinCodeCharFour == 'R') {
                return PRODUCT_MAP_CHART_VALS.get(String.valueOf(vinYearCharFive)) != null
                        ? PRODUCT_MAP_CHART_VALS.get(String.valueOf(vinYearCharFive))
                        : PRODUCT_RANGER;
            } else {
                return "";
            }
        }

    }

    private String getModel(String vinNumber) {
        String vinCodeOneToThree = vinNumber.substring(0, VIN_DECODE_SUBSTRING_THREE);
        char vinCodeCharFour = vinNumber.charAt(VIN_DECODE_SUBSTRING_THREE);
        char vinYearCharFive = vinNumber.charAt(VIN_DECODE_SUBSTRING_FOUR);
        String vinYearCharSixthtoSeven = vinNumber.substring(VIN_DECODE_SUBSTRING_SIX, VIN_DECODE_SUBSTRING_EIGHT);
        if (vinCodeOneToThree.equals("56K")) {
            if (vinCodeCharFour == 'C') {
                return vinYearCharFive == 'C' ? MODEL_CHIEF : (vinYearCharFive == 'V' ? MODEL_VINTAGE : "");
            } else if (vinCodeCharFour == 'T') {
                return vinNumber = MODEL_MAP_CHART_VAL.get(String.valueOf(vinYearCharFive)) != null
                        ? MODEL_MAP_CHART_VAL.get(String.valueOf(vinYearCharFive))
                        : "";
            }
            return "";
        } else {
            if (vinCodeCharFour == 'D' && vinYearCharSixthtoSeven.equals("32")) {
                return MODEL_325;
            } else if (vinCodeCharFour == 'S' && vinYearCharSixthtoSeven.equals("50")) {
                return MODEL_450;
            } else {
                return vinYearCharSixthtoSeven.equals("5C") ? MODEL_450
                        : MODEL_MAP_CHART_VALS.get(vinYearCharSixthtoSeven) != null
                                ? MODEL_MAP_CHART_VALS.get(vinYearCharSixthtoSeven)
                                : "";
            }
        }
    }

    private String getYear(String vinNumber) {
        return YEAR_MAP_CHART_VAL.get(String.valueOf(vinNumber.charAt(VIN_DECODE_SUBSTRING_NINE))) != null
                ? YEAR_MAP_CHART_VAL.get(String.valueOf(vinNumber.charAt(VIN_DECODE_SUBSTRING_NINE)))
                : "";
    }
}
