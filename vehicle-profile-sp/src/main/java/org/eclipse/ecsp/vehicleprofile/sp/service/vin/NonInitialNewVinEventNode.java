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
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;
import org.eclipse.ecsp.vehicleprofile.sp.utils.SpCommonConstants;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class to handle VIN events for a new valid VIN on subsequent ignition on.
 * This is the seventh node in the chain of responsibility pattern.
 *
 */
@Service(value = "NonInitialNewVinEventNode")
public class NonInitialNewVinEventNode extends DeviceVinEventChain {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(NonInitialNewVinEventNode.class);

    /**
     * the next node in the chain of type {@link RepeatedVinEventNode}.
     */
    @Autowired
    @Qualifier(value = "RepeatedVinEventNode")
    private DeviceVinEventChain nextNode;

    @Override
    public void processVinEvent(String deviceId, DeviceVinEventDataV1_0 eventData, VehicleProfile vpByDeviceId,
            VehicleProfile vpByReceivedVin, VehicleProfile deviceDummyVinProfile, AtomicBoolean mmyAlertFlag,
            AtomicBoolean vinChangeAlertFlag) throws URISyntaxException {

        if (!eventData.isDummy() && Optional.ofNullable(vpByDeviceId).isPresent()
                && !vpByDeviceId.getVin().equals(eventData.getValue())) {

            LOGGER.info("Received subsequent event with new valid VIN: {} for device: {} ...",
                    CommonUtils.maskContent(eventData.getValue()), CommonUtils.maskContent(deviceId));

            // Remove device from current active vehicle profile.
            vpByDeviceId.getEcus().remove(eventData.getDeviceType());
            vpByDeviceId.setUpdatedOn(new Date());
            vpByDeviceId.setModemInfo(null);
            vpApiCallService.updateVehicleProfile(vpByDeviceId.getVehicleId(), vpByDeviceId);

            // Move Device, from current active Valid VIN profile to created new
            // Valid VIN profile.
            VehicleProfile newVehicleProfile = createValidVinProfile(deviceId, eventData);
            newVehicleProfile.setAuthorizedUsers(vpByDeviceId.getAuthorizedUsers());
            vpApiCallService.createVehicleProfile(newVehicleProfile);
            // change in vin noticed, Notifcation will send to user
            vinChangeAlertFlag.set(true);
            mmyAlertFlag
                    .set(SpCommonConstants.NOT_APPLICABLE.equals(newVehicleProfile.getVehicleAttributes().getMake()));

        } else {
            nextNode.processVinEvent(deviceId, eventData, vpByDeviceId, vpByReceivedVin, deviceDummyVinProfile,
                    mmyAlertFlag, vinChangeAlertFlag);
        }
    }

}
