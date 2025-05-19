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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class to handle VIN events having a dummy VIN on subsequent ignition on. This
 * is the fourth node in the chain of responsibility pattern.
 *
 */
@Service(value = "NonInitialDummyVinEventNode")
public class NonInitialDummyVinEventNode extends DeviceVinEventChain {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(NonInitialDummyVinEventNode.class);

    /**
     * the next node in the chain of type {@link InitialExistingVinEventNode}.
     */
    @Autowired
    @Qualifier(value = "InitialExistingVinEventNode")
    private DeviceVinEventChain nextNode;

    @Override
    public void processVinEvent(String deviceId, DeviceVinEventDataV1_0 eventData, VehicleProfile vpByDeviceId,
            VehicleProfile vpByReceivedVin, VehicleProfile deviceDummyVinProfile, AtomicBoolean mmyAlertFlag,
            AtomicBoolean vinChangeAlertFlag) throws URISyntaxException {

        if (eventData.isDummy() && Optional.ofNullable(vpByDeviceId).isPresent()
                && !vpByDeviceId.getVin().startsWith(SpCommonConstants.VIN_CONVERTED_DUMMY)
                && Optional.ofNullable(deviceDummyVinProfile).isPresent()) {

            LOGGER.info("Received subsequent event with a dummy VIN for device: {} ...",
                    CommonUtils.maskContent(deviceId));

            // Remove device from the current active VIN profile
            Date updatedNow = new Date();
            vpByDeviceId.getEcus().remove(eventData.getDeviceType());
            vpByDeviceId.setUpdatedOn(updatedNow);
            vpByDeviceId.setModemInfo(null);

            vpApiCallService.updateVehicleProfile(vpByDeviceId.getVehicleId(), vpByDeviceId);

            // Move device to its dummy VIN profile
            Map<String, Ecu> ecuMap = Optional.ofNullable(deviceDummyVinProfile.getEcus()).isPresent()
                    ? (Map<String, Ecu>) deviceDummyVinProfile.getEcus()
                    : new HashMap<>();
            Ecu ecu = new Ecu();
            ecu.setClientId(deviceId);
            ecuMap.put(eventData.getDeviceType(), ecu);

            deviceDummyVinProfile.setEcus(ecuMap);
            if (deviceDummyVinProfile.getModemInfo() == null) {
                deviceDummyVinProfile.setModemInfo(deviceAssociationService.getDeviceDetailsByDeviceId(deviceId));
            }

            // Move Device ,Carry forward MMY, Car name, Base color, Body type
            // from current Valid VIN profile to Dummy profile.
            deviceDummyVinProfile.getVehicleAttributes().setBodyType(vpByDeviceId.getVehicleAttributes().getBodyType());
            deviceDummyVinProfile.getVehicleAttributes()
                    .setBaseColor(vpByDeviceId.getVehicleAttributes().getBaseColor());
            deviceDummyVinProfile.getVehicleAttributes().setMake(vpByDeviceId.getVehicleAttributes().getMake());
            deviceDummyVinProfile.getVehicleAttributes().setModel(vpByDeviceId.getVehicleAttributes().getModel());
            deviceDummyVinProfile.getVehicleAttributes()
                    .setModelYear(vpByDeviceId.getVehicleAttributes().getModelYear());
            deviceDummyVinProfile.getVehicleAttributes().setFuelType(vpByDeviceId.getVehicleAttributes().getFuelType());
            deviceDummyVinProfile.getVehicleAttributes().setType(vpByDeviceId.getVehicleAttributes().getType());
            deviceDummyVinProfile.getVehicleAttributes().setName(vpByDeviceId.getVehicleAttributes().getName());
            deviceDummyVinProfile.setUpdatedOn(updatedNow);

            vpApiCallService.updateVehicleProfile(deviceDummyVinProfile.getVehicleId(), deviceDummyVinProfile);

        } else {
            nextNode.processVinEvent(deviceId, eventData, vpByDeviceId, vpByReceivedVin, deviceDummyVinProfile,
                    mmyAlertFlag, vinChangeAlertFlag);
        }
    }

}