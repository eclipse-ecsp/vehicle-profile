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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Inventory.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Inventory {

    @JsonProperty(value = "inventoryversion")
    private String inventoryversion;

    @JsonProperty(value = "vin")
    private String vin;

    @JsonProperty(value = "timestamp")
    private Date timestamp;

    @JsonProperty(value = "campaignid")
    private String campaignid;

    Map<String, InventoryEcu> inventoryEcuMap = new LinkedHashMap<>();

    /**
     * Get inventory vesrion.
     *
     * @return string
     */
    public String getInventoryversion() {
        return inventoryversion;
    }

    /**
     * Set inventory version.
     *
     * @param inventoryversion string
     */
    public void setInventoryversion(String inventoryversion) {
        this.inventoryversion = inventoryversion;
    }

    /**
     * Get vin.
     *
     * @return string
     */
    public String getVin() {
        return vin;
    }

    /**
     * Set vin.
     *
     * @param vin string
     */
    public void setVin(String vin) {
        this.vin = vin;
    }

    /**
     * Get timestamp.
     *
     * @return date
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Set timestamp.
     *
     * @param timestamp date
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Get campaign id.
     *
     * @return string
     */
    public String getCampaignid() {
        return campaignid;
    }

    /**
     * Set campaign id.
     *
     * @param campaignid string
     */
    public void setCampaignid(String campaignid) {
        this.campaignid = campaignid;
    }

    /**
     * Get inventory ecu map.
     *
     * @return map
     */
    public Map<String, InventoryEcu> getInventoryEcuMap() {
        return inventoryEcuMap;
    }

    /**
     * Set inventory ecu map.
     *
     * @param inventoryEcuMap map
     */
    public void setInventoryEcuMap(Map<String, InventoryEcu> inventoryEcuMap) {
        this.inventoryEcuMap = inventoryEcuMap;
    }

    /**
     * Set dynamic property.
     *
     * @param key string
     * @param ecu InventoryEcu
     */
    @JsonAnySetter
    public void setDynamicProp(String key, InventoryEcu ecu) {
        inventoryEcuMap.put(key, ecu);
    }

    /**
     * toString.
     *
     * @return string
     */
    @Override
    public String toString() {
        return "Inventory [inventoryversion=" + inventoryversion + ", vin=" + vin + ", timestamp=" + timestamp
                + ", campaignid=" + campaignid + ", inventoryEcuMap=" + inventoryEcuMap + "]";
    }

    /**
     * maskedToString.
     *
     * @return string
     */
    public String maskedToString() {
        return "Inventory [inventoryversion=" + inventoryversion + ", vin=" + CommonUtils.maskContent(vin)
                + ", timestamp=" + timestamp + ", campaignid=" + campaignid + ", inventoryEcuMap=" + inventoryEcuMap
                + "]";
    }

}
