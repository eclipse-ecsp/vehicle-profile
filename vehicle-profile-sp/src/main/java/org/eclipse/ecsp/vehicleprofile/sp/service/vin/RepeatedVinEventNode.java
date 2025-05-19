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
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class to handle event for repeated VIN-device id combination on subsequent
 * ignition on. This is the last node in the chain of responsibility pattern.
 *
 */
@Service(value = "RepeatedVinEventNode")
public class RepeatedVinEventNode extends DeviceVinEventChain {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(RepeatedVinEventNode.class);

    @Override
    public void processVinEvent(String deviceId, DeviceVinEventDataV1_0 eventData, VehicleProfile vpByDeviceId,
            VehicleProfile vpByReceivedVin, VehicleProfile deviceDummyVinProfile, AtomicBoolean mmyAlertFlag,
            AtomicBoolean vinChangeAlertFlag) throws URISyntaxException {

        // Check if the VIN event is the same as earlier VIN event i.e same
        // Device and VIN combination.

        if (!eventData.isDummy() && Optional.ofNullable(vpByDeviceId).isPresent()
                && vpByDeviceId.getVin().equals(eventData.getValue())) {

            LOGGER.info("Received repeated VIN event for same VIN & Device Id combination ....");

            vpByDeviceId.setUpdatedOn(new Date());

            vpApiCallService.updateVehicleProfile(vpByDeviceId.getVehicleId(), vpByDeviceId);

        } else {
            LOGGER.info("Could not process the VIN event in any of the nodes in the chain. Ignoring event ...");
        }

    }

}
