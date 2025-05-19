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

package org.eclipse.ecsp.vehicleprofile.notifier.events;

import org.eclipse.ecsp.annotations.EventMapping;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.entities.AbstractEventData;
import org.eclipse.ecsp.vehicleprofile.constants.Constants;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;

/**
 * VehicleProfileCreatedNotificationEventDataV1 class.
 */
@EventMapping(id = Constants.VEHICLE_PROFILE_CREATED_NOTIFICATION_EVENT, version = Version.V1_1)
public class VehicleProfileCreatedNotificationEventDataV1 extends AbstractEventData {

    private static final long serialVersionUID = -339997586322190202L;

    private VehicleProfile vehicleProfile;

    /**
     * Gets the vehicle profile.
     *
     * @return vehicleProfile
     */
    public VehicleProfile getVehicleProfile() {
        return vehicleProfile;
    }

    /**
     * Sets the vehicle profile.
     *
     * @param vehicleProfile the new vehicle profile
     */
    public void setVehicleProfile(VehicleProfile vehicleProfile) {
        this.vehicleProfile = vehicleProfile;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "VehicleProfileCreatedNotificationEventDataV1_1 [vehicleProfile=" + vehicleProfile + "]";
    }
}
