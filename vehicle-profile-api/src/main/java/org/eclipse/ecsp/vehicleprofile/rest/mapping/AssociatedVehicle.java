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

package org.eclipse.ecsp.vehicleprofile.rest.mapping;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;

/**
 *  AssociatedVehicle class.
 */
@Getter
@Setter
public class AssociatedVehicle {

    private String vehicleId;
    private String role;

    /**
     * Associated Vehicle Constructor.
     */
    public AssociatedVehicle() {
        super();
    }

    /**
     * Associated vehicle.
     *
     * @param vehicleId string
     * @param role string
     */
    public AssociatedVehicle(String vehicleId, String role) {
        super();
        this.vehicleId = vehicleId;
        this.role = role;
    }

    @Override
    public String toString() {
        return "AssociatedVehicle [" + "vehicleId=" + CommonUtils.maskContent(vehicleId) + ", role=" + role + "]";
    }

}