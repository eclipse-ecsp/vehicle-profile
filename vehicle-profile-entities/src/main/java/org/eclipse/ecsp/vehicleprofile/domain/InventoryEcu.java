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

package org.eclipse.ecsp.vehicleprofile.domain;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * InventoryEcu.
 */
public class InventoryEcu {

    @JsonProperty(value = "partnumber")
    private String partNumber;

    @JsonProperty(value = "hardware")
    private InventoryEcuHardware inventoryEcuHardware;

    private Map<String, Map<String, InventoryScomo>> inventoryScomoMap = new LinkedHashMap<>();

    /**
     * Get part number.
     *
     * @return string
     */
    public String getPartNumber() {
        return partNumber;
    }

    /**
     * Set part number.
     *
     * @param partNumber string
     */
    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    /**
     * Get inventory ecu hardware.
     *
     * @return InventoryEcuHardware
     */
    public InventoryEcuHardware getInventoryEcuHardware() {
        return inventoryEcuHardware;
    }

    /**
     * Set inventory ecu hardware.
     *
     * @param inventoryEcuHardware InventoryEcuHardware
     */
    public void setInventoryEcuHardware(InventoryEcuHardware inventoryEcuHardware) {
        this.inventoryEcuHardware = inventoryEcuHardware;
    }

    /**
     * Get inventory scomo map.
     *
     * @return map
     */
    public Map<String, Map<String, InventoryScomo>> getInventoryScomoMap() {
        return inventoryScomoMap;
    }

    /**
     * Set inventory scomo map.
     *
     * @param inventoryScomoMap map
     */
    public void setInventoryScomoMap(Map<String, Map<String, InventoryScomo>> inventoryScomoMap) {
        this.inventoryScomoMap = inventoryScomoMap;
    }

    /**
     * Set dynamic property.
     *
     * @param key      string
     * @param scomoMap map
     */
    @JsonAnySetter
    public void setDynamicProperty(String key, Map<String, InventoryScomo> scomoMap) {
        inventoryScomoMap.put(key, scomoMap);
    }

    @Override
    public String toString() {
        return "InventoryEcu [partNumber=" + CommonUtils.maskContent(partNumber) + ", inventoryEcuHardware="
                + inventoryEcuHardware + ", inventoryScomoMap=" + inventoryScomoMap + "]";
    }

}
