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

package org.eclipse.ecsp.events.vehicleprofile;

import org.eclipse.ecsp.annotations.EventMapping;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.entities.AbstractEventData;
import org.eclipse.ecsp.events.vehicleprofile.constants.Constants;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import lombok.EqualsAndHashCode;

/**
 * DeviceVinEventDataV1_0.
 */
@EventMapping(id = Constants.VIN_EVENT_ID, version = Version.V1_0)
@EqualsAndHashCode
public class DeviceVinEventDataV1_0 extends AbstractEventData {

    private static final long serialVersionUID = 1L;

    /**
     * value.
     */
    private String value;

    /**
     * dummy.
     */
    private boolean dummy;

    /**
     * type.
     */
    private String type;

    /**
     * userId.
     */
    private String userId;

    /**
     * modelName.
     */
    private String modelName;

    /**
     * deviceType.
     */
    private String deviceType;

    /**
     * Instantiates a new device vin event data v1_0.
     */
    public DeviceVinEventDataV1_0() {
        super();
    }

    /**
     * Gets the value.
     *
     * @return the string
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value.
     *
     * @param value the new value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Checks if is dummy.
     *
     * @return true, if is dummy
     */
    public boolean isDummy() {
        return dummy;
    }

    /**
     * Sets the dummy.
     *
     * @param dummy the new dummy
     */
    public void setDummy(boolean dummy) {
        this.dummy = dummy;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type the new type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the user id.
     *
     * @return the string
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user id.
     *
     * @param userId the new user id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the model name.
     *
     * @return the model name
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Sets the model name.
     *
     * @param modelName the new model name
     */
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    /**
     * Gets the device type.
     *
     * @return the string
     */
    public String getDeviceType() {
        return deviceType;
    }

    /**
     * Sets the device type.
     *
     * @param deviceType the new device type
     */
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    @Override
    public String toString() {
        return "DeviceVinEventDataV1_0{" + "value='" + (dummy ? value : CommonUtils.maskContent(value)) + '\''
                + ", dummy=" + dummy + ", type='" + type + '\'' + ", userId='" + CommonUtils.maskContent(userId) + '\''
                + ", modelName='" + modelName + '\'' + ", deviceType='" + deviceType + '\'' + '}';
    }
}