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

package org.eclipse.ecsp.vehicleprofile.sp.service.vin;

import org.eclipse.ecsp.events.vehicleprofile.DeviceVinEventDataV1_0;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.domain.Ecu;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;
import org.eclipse.ecsp.vehicleprofile.sp.utils.SpCommonConstants;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * DeviceReactivateNode.
 */
@Service(value = "DeviceReactivateNode")
public class DeviceReactivateNode extends DeviceVinEventChain {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(DeviceReactivateNode.class);

    /**
     * nextNode in the chain of type {@link InitialDummyVinEventNode}.
     */
    @Autowired
    @Qualifier(value = "InitialDummyVinEventNode")
    private DeviceVinEventChain nextNode;

    /**
     * process Vin Event.
     */
    @Override
    public void processVinEvent(String deviceId, DeviceVinEventDataV1_0 eventData, VehicleProfile vpByDeviceId,
            VehicleProfile vpByReceivedVin, VehicleProfile deviceDummyVinProfile, AtomicBoolean mmyAlertFlag,
            AtomicBoolean vinChangeAlertFlag) throws URISyntaxException {

        if (SpCommonConstants.HCP_GENERATED_DUMMY.equals(eventData.getValue())
                && Optional.ofNullable(vpByDeviceId).isPresent()) {

            if (Optional.ofNullable(eventData.getDeviceType()).isPresent()) {

                if (!vpByDeviceId.getEcus().containsKey(eventData.getDeviceType())) {
                    LOGGER.info("Updating VehicleArchType and deviceType for a device {} ...",
                            CommonUtils.maskContent(deviceId));
                    if (StringUtils.isNotBlank(eventData.getDeviceType())) {
                        Map<String, Ecu> ecuMap = (Map<String, Ecu>) vpByDeviceId.getEcus();
                        String oldDeviceType = "";
                        for (Map.Entry<String, Ecu> entry : ecuMap.entrySet()) {
                            if (entry.getValue().getClientId().equalsIgnoreCase(deviceId)) {
                                oldDeviceType = entry.getKey();
                                break;
                            }
                        }
                        if (StringUtils.isNotBlank(oldDeviceType)) {
                            ecuMap.put(eventData.getDeviceType(), ecuMap.remove(oldDeviceType));
                            vpByDeviceId.setVehicleArchType(eventData.getDeviceType());
                            vpByDeviceId.setEcus(ecuMap);
                        }
                    }
                    vpApiCallService.updateVehicleProfile(vpByDeviceId.getVehicleId(), vpByDeviceId);
                }
            }
        } else {
            nextNode.processVinEvent(deviceId, eventData, vpByDeviceId, vpByReceivedVin, deviceDummyVinProfile,
                    mmyAlertFlag, vinChangeAlertFlag);
        }
    }
}
