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
import dev.morphia.annotations.Entity;

/**
 * DeviceMessageData.
 */
@Entity
public class DeviceMessageData {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "DisplacementCC")
    private Integer displacementCc;

    @JsonProperty(value = "PowerPsAtRpm")
    private Integer powerPsAtRpm;

    @JsonProperty(value = "FuelType")
    private Integer fuelType;

    @JsonProperty(value = "TankCapacity")
    private Integer tankCapacity;

    @JsonProperty(value = "ServiceMaintenanceId")
    private String serviceMaintenanceId;

    @JsonProperty(value = "EPIDDBChecksum")
    private String epiddbChecksum;

    /**
     * Get displacement.
     *
     * @return integer
     */
    public Integer getDisplacementCc() {
        return displacementCc;
    }

    /**
     * Set displacement.
     *
     * @param displacementCc integer
     */
    public void setDisplacementCc(Integer displacementCc) {
        this.displacementCc = displacementCc;
    }

    /**
     * Get power PS at RPM.
     *
     * @return integer
     */
    public Integer getPowerPsAtRpm() {
        return powerPsAtRpm;
    }

    /**
     * Set power PS at RPM.
     *
     * @param powerPsAtRpm integer
     */
    public void setPowerPsAtRpm(Integer powerPsAtRpm) {
        this.powerPsAtRpm = powerPsAtRpm;
    }

    /**
     * Get fuel type.
     *
     * @return integer
     */
    public Integer getFuelType() {
        return fuelType;
    }

    /**
     * Set fuel type.
     *
     * @param fuelType integer
     */
    public void setFuelType(Integer fuelType) {
        this.fuelType = fuelType;
    }

    /**
     * Get tank capacity.
     *
     * @return integer
     */
    public Integer getTankCapacity() {
        return tankCapacity;
    }

    /**
     * Set tank capacity.
     *
     * @param tankCapacity integer
     */
    public void setTankCapacity(Integer tankCapacity) {
        this.tankCapacity = tankCapacity;
    }

    /**
     * Get service maintenance id.
     *
     * @return string
     */
    public String getServiceMaintenanceId() {
        return serviceMaintenanceId;
    }

    /**
     * Set service maintenance id.
     *
     * @param serviceMaintenanceId string
     */
    public void setServiceMaintenanceId(String serviceMaintenanceId) {
        this.serviceMaintenanceId = serviceMaintenanceId;
    }

    /**
     * Get serial version UID.
     *
     * @return long
     */
    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    /**
     * Get EPIDDB checksum.
     *
     * @return string
     */
    public String getEpiddbChecksum() {
        return epiddbChecksum;
    }

    /**
     * Set EPIDDB checksum.
     *
     * @param epiddbChecksum string
     */
    public void setEpiddbChecksum(String epiddbChecksum) {
        this.epiddbChecksum = epiddbChecksum;
    }

    @Override
    public String toString() {
        return "DeviceMessageData [displacementCc=" + displacementCc + ", powerPsAtRpm=" + powerPsAtRpm + ", fuelType="
                + fuelType + ", tankCapacity=" + tankCapacity + ", serviceMaintenanceId=" + serviceMaintenanceId
                + ", epiddbChecksum=" + epiddbChecksum + "]";
    }

}
