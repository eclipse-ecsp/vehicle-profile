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

import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import java.util.Map;

/**
 * vehicleProfileEcuFilterRequest.
 */
public class VehicleProfileEcuFilterRequest {

    private String vehicleId;
    private Map<String, ? extends EcusFilterDto> ecus;
    private String deviceType;
    private String connectedPlatform;

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Map<String, ? extends EcusFilterDto> getEcus() {
        return ecus;
    }

    public void setEcus(Map<String, ? extends EcusFilterDto> ecus) {
        this.ecus = ecus;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }


    public String getConnectedPlatform() {
        return connectedPlatform;
    }

    public void setConnectedPlatform(String connectedPlatform) {
        this.connectedPlatform = connectedPlatform;
    }

    @Override
    public String toString() {
        return "VehicleProfile [" + (vehicleId != null ? "vehicleId=" + vehicleId + ", " : "")
                + (deviceType != null ? "deviceType=" + deviceType + ", " : "")
                + (connectedPlatform != null ? "connectedPlatform=" + connectedPlatform + ", " : "")
                + (ecus != null ? "ecus=" + ecus + ", " : "")  + "]";
    }

    /**
     * maskedToString.
     */
    public String maskedToString() {
        return "VehicleProfile [" + (vehicleId != null ? "vehicleId=" + CommonUtils.maskContent(vehicleId) + ", " : "")
                + (deviceType != null ? "deviceType=" + deviceType + ", " : "")
                + (connectedPlatform != null ? "connectedPlatform=" + connectedPlatform + ", " : "")
                + (ecus != null ? "ecus=" + ecus + ", " : "") + "]";
    }
}