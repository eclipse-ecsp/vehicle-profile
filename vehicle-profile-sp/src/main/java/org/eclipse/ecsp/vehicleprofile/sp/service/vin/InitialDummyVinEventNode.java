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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class to handle dummy VIN event sent by the Dongle on first ignition on. This
 * is the second node in the chain of responsibility pattern.
 *
 */
@Service(value = "InitialDummyVinEventNode")
public class InitialDummyVinEventNode extends DeviceVinEventChain {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(InitialDummyVinEventNode.class);

    /**
     * nextNode in the chain of type {@link InitialNewVinEventNode}.
     */

    @Autowired
    @Qualifier(value = "InitialNewVinEventNode")
    private DeviceVinEventChain nextNode;

    @Override
    public void processVinEvent(String deviceId, DeviceVinEventDataV1_0 eventData, VehicleProfile vpByDeviceId,
            VehicleProfile vpByReceivedVin, VehicleProfile deviceDummyVinProfile, AtomicBoolean mmyAlertFlag,
            AtomicBoolean vinChangeAlertFlag) throws URISyntaxException {

        if (eventData.isDummy() && Optional.ofNullable(vpByDeviceId).isPresent()
                && (vpByDeviceId.getVin().startsWith(SpCommonConstants.HCP_GENERATED_DUMMY)
                        || vpByDeviceId.getVin().startsWith(SpCommonConstants.VIN_CONVERTED_DUMMY))) {

            LOGGER.info("Received initial Vin Event as dummy for device: {} ...", CommonUtils.maskContent(deviceId));

            boolean isHcptoVinConverted = convertHcpToVinProfile(vpByDeviceId);

            // Notification will be sent only once when the HCP profile is
            // converted to dummy VIN profile in case MMY is NA.
            mmyAlertFlag.set(SpCommonConstants.NOT_APPLICABLE.equals(vpByDeviceId.getVehicleAttributes().getMake())
                    && isHcptoVinConverted);
        } else {
            nextNode.processVinEvent(deviceId, eventData, vpByDeviceId, vpByReceivedVin, deviceDummyVinProfile,
                    mmyAlertFlag, vinChangeAlertFlag);
        }

    }

}