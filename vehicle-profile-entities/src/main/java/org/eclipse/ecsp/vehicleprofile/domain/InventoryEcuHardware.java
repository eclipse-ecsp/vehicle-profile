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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * InventoryEcuHardware.
 */
public class InventoryEcuHardware {

    @JsonProperty(value = "version")
    private String version;

    @JsonProperty(value = "serialnumber")
    private String serialNumber;

    /**
     * Get version.
     *
     * @return string
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set version.
     *
     * @param version string
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Get serial number.
     *
     * @return string
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * Set serial number.
     *
     * @param serialNumber string
     */
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    public String toString() {
        return "InventoryEcuHardware [version=" + version + ", serialNumber=" + serialNumber + "]";
    }

}
