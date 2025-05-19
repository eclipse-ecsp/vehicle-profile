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
import org.springframework.util.StringUtils;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class to handle VIN events for an existing VIN on first ignition on. This is
 * the fifth node in the chain of responsibility pattern.
 *
 */
@Service(value = "InitialExistingVinEventNode")

public class InitialExistingVinEventNode extends DeviceVinEventChain {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(InitialExistingVinEventNode.class);

    /**
     * nextNode in the chain of type {@link NonInitialExistingVinEventNode}.
     */
    @Autowired
    @Qualifier(value = "NonInitialExistingVinEventNode")
    private DeviceVinEventChain nextNode;

    @Override
    public void processVinEvent(String deviceId, DeviceVinEventDataV1_0 eventData, VehicleProfile vpByDeviceId,
            VehicleProfile vpByReceivedVin, VehicleProfile deviceDummyVinProfile, AtomicBoolean mmyAlertFlag,
            AtomicBoolean vinChangeAlertFlag) throws URISyntaxException {

        if (!eventData.isDummy() && Optional.ofNullable(vpByReceivedVin).isPresent()
                && Optional.ofNullable(vpByDeviceId).isPresent()
                && (vpByDeviceId.getVin().startsWith(SpCommonConstants.HCP_GENERATED_DUMMY)
                        || vpByDeviceId.getVin().startsWith(SpCommonConstants.VIN_CONVERTED_DUMMY))) {

            LOGGER.info("Received initial Vin Event which is existing vin for device: {} ...",
                    CommonUtils.maskContent(deviceId));

            // Remove device from the current active VIN profile
            vpByDeviceId.getEcus().remove(eventData.getDeviceType());
            convertHcpToVinProfile(vpByDeviceId);

            // Move device already associated to the received VIN to its dummy
            // profile.
            moveDeviceToDummyProfile(vpByReceivedVin, eventData.getDeviceType());

            // Move device to the received VIN profile
            Map<String, Ecu> ecuMap = Optional.ofNullable(vpByReceivedVin.getEcus()).isPresent()
                    ? (Map<String, Ecu>) vpByReceivedVin.getEcus()
                    : new HashMap<>();
            Ecu ecu = new Ecu();
            ecu.setClientId(deviceId);
            ecuMap.put(eventData.getDeviceType(), ecu);

            vpByReceivedVin.setEcus(ecuMap);
            vpByReceivedVin.setModemInfo(vpByDeviceId.getModemInfo());
            // Carry Car name, Base color, Body type from dummy VIN
            // profile to received VIN
            // profile.
            vpByReceivedVin.getVehicleAttributes().setBodyType(vpByDeviceId.getVehicleAttributes().getBodyType());
            vpByReceivedVin.getVehicleAttributes().setBaseColor(vpByDeviceId.getVehicleAttributes().getBaseColor());
            if (!StringUtils.isEmpty(vpByDeviceId.getVehicleAttributes().getName())
                    && !SpCommonConstants.MY_CAR.equals(vpByDeviceId.getVehicleAttributes().getName())) {
                vpByReceivedVin.getVehicleAttributes().setName(vpByDeviceId.getVehicleAttributes().getName());
            }
            vpByReceivedVin.setUpdatedOn(new Date());
            vpByReceivedVin.setAuthorizedUsers(vpByDeviceId.getAuthorizedUsers());
            vinChangeAlertFlag.set(true);
            vpApiCallService.updateVehicleProfile(vpByReceivedVin.getVehicleId(), vpByReceivedVin);

        } else {
            nextNode.processVinEvent(deviceId, eventData, vpByDeviceId, vpByReceivedVin, deviceDummyVinProfile,
                    mmyAlertFlag, vinChangeAlertFlag);
        }
    }

}