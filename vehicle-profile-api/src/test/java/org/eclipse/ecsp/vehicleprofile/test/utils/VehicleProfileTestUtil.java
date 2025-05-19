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

package org.eclipse.ecsp.vehicleprofile.test.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;

import java.net.URL;

/**
 * VehicleProfileTestUtil.
 */
public class VehicleProfileTestUtil {

    private VehicleProfileTestUtil() {}
    
    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * generateVehicleProfile.
     */
    public static VehicleProfile generateVehicleProfile() {

        VehicleProfile vehicleProfile = null;
        try {
            vehicleProfile = objectMapper.readValue(new URL("classpath:vehicle-profile.json"), VehicleProfile.class);
            vehicleProfile.setVin(String.valueOf(System.nanoTime()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vehicleProfile;

    }
}
