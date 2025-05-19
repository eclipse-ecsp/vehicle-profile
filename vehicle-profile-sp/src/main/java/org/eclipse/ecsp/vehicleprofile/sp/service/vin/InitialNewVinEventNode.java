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
import org.springframework.util.StringUtils;

import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class to handle VIN events with a new valid VIN on first ignition on. This is
 * the third node in the chain of responsibility pattern.
 *
 */
@Service(value = "InitialNewVinEventNode")
public class InitialNewVinEventNode extends DeviceVinEventChain {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(InitialNewVinEventNode.class);

    /**
     * nextNode in the chain of type {@link NonInitialDummyVinEventNodes}.
     */
    @Autowired
    @Qualifier(value = "NonInitialDummyVinEventNode")
    private DeviceVinEventChain nextNode;

    @Override
    public void processVinEvent(String deviceId, DeviceVinEventDataV1_0 eventData, VehicleProfile vpByDeviceId,
            VehicleProfile vpByReceivedVin, VehicleProfile deviceDummyVinProfile, AtomicBoolean mmyAlertFlag,
            AtomicBoolean vinChangeAlertFlag) throws URISyntaxException {

        if (!eventData.isDummy() && Optional.ofNullable(vpByDeviceId).isPresent()
                && (vpByDeviceId.getVin().startsWith(SpCommonConstants.HCP_GENERATED_DUMMY)
                        || vpByDeviceId.getVin().startsWith(SpCommonConstants.VIN_CONVERTED_DUMMY))
                && !Optional.ofNullable(vpByReceivedVin).isPresent()) {

            LOGGER.info("Received initial Vin Event as new valid VIN: {} for device: {} ...",
                    CommonUtils.maskContent(eventData.getValue()), CommonUtils.maskContent(deviceId));

            // Remove device from the current active profile
            vpByDeviceId.getEcus().remove(eventData.getDeviceType());
            convertHcpToVinProfile(vpByDeviceId);

            // Move Device, Carry forward Car name, Base color, Body type from
            // dummy VIN profile to new Valid VIN profile.
            VehicleProfile newVehicleProfile = createValidVinProfile(deviceId, eventData);

            if (newVehicleProfile.getModemInfo() == null) {
                newVehicleProfile.setModemInfo(vpByDeviceId.getModemInfo());
            }
            // Copy Car Name, Base Color, Body Type from initial dummy profile
            // to new profile
            if (!StringUtils.isEmpty(vpByDeviceId.getVehicleAttributes().getName())
                    && !SpCommonConstants.MY_CAR.equals(vpByDeviceId.getVehicleAttributes().getName())) {
                newVehicleProfile.getVehicleAttributes().setName(vpByDeviceId.getVehicleAttributes().getName());
            }
            newVehicleProfile.getVehicleAttributes().setBaseColor(vpByDeviceId.getVehicleAttributes().getBaseColor());
            newVehicleProfile.getVehicleAttributes().setBodyType(vpByDeviceId.getVehicleAttributes().getBodyType());
            newVehicleProfile.setAuthorizedUsers(vpByDeviceId.getAuthorizedUsers());
            vpApiCallService.createVehicleProfile(newVehicleProfile);
            vinChangeAlertFlag.set(true);
            mmyAlertFlag.set(SpCommonConstants.NOT_APPLICABLE.equals(newVehicleProfile.getVehicleAttributes().getMake())
                    || SpCommonConstants.UNKNOWN_VEHICLE.equals(newVehicleProfile.getVehicleAttributes().getType()));

        } else {
            nextNode.processVinEvent(deviceId, eventData, vpByDeviceId, vpByReceivedVin, deviceDummyVinProfile,
                    mmyAlertFlag, vinChangeAlertFlag);
        }
    }

}
