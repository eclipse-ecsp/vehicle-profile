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

import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.domain.Ecu;
import org.eclipse.ecsp.vehicleprofile.domain.InventoryEcu;
import org.eclipse.ecsp.vehicleprofile.domain.InventoryScomo;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

/**
 * Utility class.
 */
@Component
public class Util {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(Util.class);

    @Value("#{'${allowed.device.types}'.split(',')}")
    private Set<String> allowedEcus;

    /**
     * hasValidECUs.
     */
    public boolean hasValidEcus(VehicleProfile vehicleProfile) {
        if (null == vehicleProfile.getEcus()) {
            return true;
        }
        Iterator<Entry<String, Ecu>> ecuItr = ((Map<String, Ecu>) vehicleProfile.getEcus()).entrySet().iterator();
        while (ecuItr.hasNext()) {
            String ecuName = ecuItr.next().getKey();
            if (!allowedEcus.contains(ecuName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * checkEmptyNullEcuType.
     */
    public void checkEmptyNullEcuType(VehicleProfile vehicleProfile, VehicleProfile existingVehicleProfile) {
        if (null == vehicleProfile.getEcus()) {
            return;
        }
        Iterator<Entry<String, Ecu>> ecuItr = ((Map<String, Ecu>) vehicleProfile.getEcus()).entrySet().iterator();
        while (ecuItr.hasNext()) {
            Entry<String, Ecu> ecuEntry = ecuItr.next();
            String ecuType = ecuEntry.getValue().getEcuType();
            String ecuName = ecuEntry.getKey();
            String vehicleId = vehicleProfile.getVehicleId();
            if (ecuType == null || ecuType.trim().isEmpty()) {
                if (null == existingVehicleProfile) {
                    LOGGER.warn("EcuType is null/empty in ECU for vehicleId: {}, ecu mapKey: {}, ecuType: {} ",
                            vehicleId, ecuName, (null == ecuType) ? "empty" : ecuType);
                } else if (null != existingVehicleProfile.getEcus()) {
                    Ecu existingEcu = ((Map<String, Ecu>) existingVehicleProfile.getEcus()).get(ecuName);
                    String existingEcuType = "";
                    if (null != existingEcu) {
                        existingEcuType = existingEcu.getEcuType();
                    }
                    LOGGER.warn(
                            "EcuType is null/empty in ECU for vehicleId: {}, ecu mapKey: {},"
                            + " currentEcuType: {}, newEcuType: {}",
                            vehicleId, ecuName, existingEcuType, (null == ecuType) ? "empty" : ecuType);
                } else {
                    LOGGER.info("Existing vehicle profile doesn't have ecus details");
                }
            }
        }
    }
    
    /**
     * getPartNumberToScomoMap.
     */
    public Map<String, Map<String, Map<String, InventoryScomo>>> getPartNumberToScomoMap(
            Map<String, InventoryEcu> inventoryEcuMap) {
        LOGGER.info("Util getPartNumberToScomoMap method - START");
        Map<String, Map<String, Map<String, InventoryScomo>>> partNumberToScomoMap = new HashMap<>();
        if (Optional.ofNullable(inventoryEcuMap).isPresent()) {
            for (Entry<String, InventoryEcu> inventoryEcu : inventoryEcuMap.entrySet()) {
                if (null != inventoryEcu.getValue() && null != inventoryEcu.getValue().getPartNumber()) {
                    partNumberToScomoMap.put(inventoryEcu.getValue().getPartNumber(),
                            inventoryEcu.getValue().getInventoryScomoMap());
                }
            }
        }
        LOGGER.info("partNumberToScomoMap : " + partNumberToScomoMap);
        LOGGER.info("Util getPartNumberToScomoMap method - END");
        return partNumberToScomoMap;
    }

}
