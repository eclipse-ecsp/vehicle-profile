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

package org.eclipse.ecsp.vehicleprofile.commons.service.vin;

import java.util.HashMap;
import java.util.Map;

/**
 * AbstractPositionMatcherVinDecoder.
 */
public abstract class AbstractPositionMatcherVinDecoder extends AbstractInternalVinDecoder<String> {
    public static final String PRODUCT_INDIAN = "Indian";
    public static final String PRODUCT_ACE = "ACE";
    public static final String PRODUCT_RZR = "RZR";
    public static  final String PRODUCT_RZRXP = "RZR XP";
    public static final String PRODUCT_RZRXP_4 = "RZR XP 4";
    public static final String PRODUCT_GENERAL = "General";
    public static final String PRODUCT_GENERAL_4 = "General 4";
    public static final String PRODUCT_RANGER_CREW = "Ranger Crew XP";
    public static final String PRODUCT_RANGER_XP = "Ranger XP";
    public static final String PRODUCT_RANGER = "Ranger";
    public static final String PRODUCT_SCRAMBLER = "Scrambler";
    public static final String PRODUCT_SPORTSMAN = "Sportsman";

    /**
     * Model name constants.
     */
    public static  final String MODEL_CHIEF = "Chief";
    public static  final String MODEL_CHIEFTAN = "Chieftain";
    public static  final String MODEL_VINTAGE = "Vintage";
    public static  final String MODEL_CHIEFTAN_CLASSIC = "Chieftain Classic";
    public static  final String MODEL_SPRINGFIELD = "Springfield";
    public static  final String MODEL_ROADMASTER = "Roadmaster";
    public static  final String MODEL_ROADMASTER_CLASSIC = "Roadmaster Classic";

    /**
     * Model code constants.
     */
    public static  final String MODEL_ETX = "ETX";
    public static final String MODEL_325 = "325";
    public static final String MODEL_450 = "450";
    public static final String MODEL_500 = "500";
    public static final String MODEL_570 = "570";
    public static final String MODEL_850 = "850";
    public static final String MODEL_900 = "900";
    public static final String MODEL_TURBO = "Turbo";
    public static final String MODEL_1000 = "1000";

    public static final Map<String, String> PRODUCT_MAP_CHART_VAL = new HashMap<String, String>() {
        {
            put("D", PRODUCT_RZRXP);
            put("F", PRODUCT_RZRXP_4);
            put("G", "RS1");
        }
    };

    public static final Map<String, String> PRODUCT_MAP_CHART_VALS = new HashMap<String, String>() {
        {
            put("G", PRODUCT_GENERAL);
            put("H", PRODUCT_GENERAL_4);
            put("R", PRODUCT_RANGER_XP);
            put("S", PRODUCT_RANGER_CREW);
            put("P", PRODUCT_RANGER_CREW);
            put("T", PRODUCT_RANGER_XP);
            put("U", PRODUCT_RANGER_CREW);
            put("V", PRODUCT_RANGER_CREW);
        }
    };

    public static final Map<String, String> MODEL_MAP_CHART_VAL = new HashMap<String, String>() {
        {
            put("C", MODEL_CHIEFTAN);
            put("F", MODEL_CHIEFTAN_CLASSIC);
            put("H", MODEL_SPRINGFIELD);
            put("R", MODEL_ROADMASTER);
            put("B", MODEL_ROADMASTER_CLASSIC);
        }
    };

    public static  final Map<String, String> MODEL_MAP_CHART_VALS = new HashMap<String, String>() {
        {
            put("32", MODEL_ETX);
            put("45", MODEL_450);
            put("50", MODEL_500);
            put("57", MODEL_570);
            put("85", MODEL_850);
            put("87", MODEL_900);
            put("92", MODEL_TURBO);
            put("95", MODEL_1000);
            put("99", MODEL_1000);

        }
    };

    public static final Map<String, String> YEAR_MAP_CHART_VAL = new HashMap<String, String>() {
        {
            put("E", "2014");
            put("F", "2015");
            put("G", "2016");
            put("H", "2017");
            put("J", "2018");
            put("K", "2019");
            put("L", "2020");
        }
    };
}
