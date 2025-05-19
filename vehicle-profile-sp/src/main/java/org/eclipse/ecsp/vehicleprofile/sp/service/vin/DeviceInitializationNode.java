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
import org.eclipse.ecsp.vehicleprofile.domain.User;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;
import org.eclipse.ecsp.vehicleprofile.sp.utils.SpCommonConstants;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class to handle VIN event sent by the platform during device authentication /
 * association phase. This is the first node in the chain of responsibility
 * pattern.
 *
 */

@Service(value = "DeviceInitializationNode")
public class DeviceInitializationNode extends DeviceVinEventChain {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(DeviceInitializationNode.class);

    /**
     * nextNode in the chain of type {@link InitialDummyVinEventNode}.
     */

    @Autowired
    @Qualifier(value = "DeviceReactivateNode")
    private DeviceReactivateNode secondNode;

    @Override
    public void processVinEvent(String deviceId, DeviceVinEventDataV1_0 eventData, VehicleProfile vpByDeviceId,
            VehicleProfile vpByReceivedVin, VehicleProfile deviceDummyVinProfile, AtomicBoolean mmyAlertFlag,
            AtomicBoolean vinChangeAlertFlag) throws URISyntaxException {

        // Check if the VIN event is generated during device
        // activation/association and the device is non-existent in the system.
        if (SpCommonConstants.HCP_GENERATED_DUMMY.equals(eventData.getValue())
                && !Optional.ofNullable(vpByDeviceId).isPresent()) {

            LOGGER.info("Creating dummy HCP vehicle profile for device: {} ...", CommonUtils.maskContent(deviceId));

            VehicleProfile vehicleProfile = new VehicleProfile();
            createVehicleProfileDto(deviceId, eventData, vehicleProfile);

            vehicleProfile.setVin(SpCommonConstants.HCP_GENERATED_DUMMY + deviceId);

            if (StringUtils.isNotBlank(eventData.getUserId())) {
                // set user in vehicleProfile from event
                User user = new User();
                user.setUserId(eventData.getUserId());
                vehicleProfile.setAuthorizedUsers(Collections.singletonList(user));
            }

            vpApiCallService.createVehicleProfile(vehicleProfile);

        } else {
            secondNode.processVinEvent(deviceId, eventData, vpByDeviceId, vpByReceivedVin, deviceDummyVinProfile,
                    mmyAlertFlag, vinChangeAlertFlag);
        }
    }
}