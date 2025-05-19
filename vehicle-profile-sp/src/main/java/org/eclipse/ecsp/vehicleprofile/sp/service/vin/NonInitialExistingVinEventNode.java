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
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.eclipse.ecsp.vehicleprofile.sp.utils.SpCommonConstants.SPACE;

/**
 * Class to handle VIN events for valid VIN which is already existing on
 * subsequent ignition on. This is the sixth node in the chain of responsibility
 * pattern.
 *
 */
@Service(value = "NonInitialExistingVinEventNode")
public class NonInitialExistingVinEventNode extends DeviceVinEventChain {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(NonInitialExistingVinEventNode.class);

    /**
     * the next node in the chain of type {@link NonInitialNewVinEventNode}.
     */
    @Autowired
    @Qualifier(value = "NonInitialNewVinEventNode")
    private DeviceVinEventChain nextNode;

    @Override
    public void processVinEvent(String deviceId, DeviceVinEventDataV1_0 eventData, VehicleProfile vpByDeviceId,
            VehicleProfile vpByReceivedVin, VehicleProfile deviceDummyVinProfile, AtomicBoolean mmyAlertFlag,
            AtomicBoolean vinChangeAlertFlag) throws URISyntaxException {

        if (!eventData.isDummy() && Optional.ofNullable(vpByReceivedVin).isPresent()
                && Optional.ofNullable(vpByDeviceId).isPresent()
                && !vpByDeviceId.getVin().equals(eventData.getValue())) {

            LOGGER.info("Received subsequent VIN event for an existing VIN: {} for device : {}",
                    CommonUtils.maskContent(eventData.getValue()), CommonUtils.maskContent(deviceId));

            // Removing device from its current active VIN profile.
            Date updatedNow = new Date();
            vpByDeviceId.getEcus().remove(eventData.getDeviceType());
            vpByDeviceId.setModemInfo(null);
            vpByDeviceId.setUpdatedOn(updatedNow);

            vpApiCallService.updateVehicleProfile(vpByDeviceId.getVehicleId(), vpByDeviceId);

            // Moving the passive device present in the received VIN profile to
            // its dummy VIN profile.
            moveDeviceToDummyProfile(vpByReceivedVin, eventData.getDeviceType());

            // Move device to received VIN profile.
            Map<String, Ecu> ecuMap = Optional.ofNullable(vpByReceivedVin.getEcus()).isPresent()
                    ? (Map<String, Ecu>) vpByReceivedVin.getEcus()
                    : new HashMap<>();
            Ecu ecu = new Ecu();
            ecu.setClientId(deviceId);
            ecuMap.put(eventData.getDeviceType(), ecu);

            vpByReceivedVin.setEcus(ecuMap);
            vpByReceivedVin.setModemInfo(deviceAssociationService.getDeviceDetailsByDeviceId(deviceId));
            vpByReceivedVin.getVehicleAttributes()
                    .setName(vpByReceivedVin.getVehicleAttributes().getMake() + SPACE
                            + vpByReceivedVin.getVehicleAttributes().getModel() + SPACE
                            + vpByReceivedVin.getVehicleAttributes().getModelYear());
            vpByReceivedVin.setUpdatedOn(updatedNow);
            // change in vin noticed, Notifcation will send to user
            vinChangeAlertFlag.set(true);
            vpApiCallService.updateVehicleProfile(vpByReceivedVin.getVehicleId(), vpByReceivedVin);
        } else {
            nextNode.processVinEvent(deviceId, eventData, vpByDeviceId, vpByReceivedVin, deviceDummyVinProfile,
                    mmyAlertFlag, vinChangeAlertFlag);
        }
    }

}
