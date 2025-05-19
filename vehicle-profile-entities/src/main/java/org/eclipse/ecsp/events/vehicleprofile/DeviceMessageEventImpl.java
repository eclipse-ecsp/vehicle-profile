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


import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.vehicleprofile.domain.DeviceMessageData;
import lombok.ToString;

/**
 * DeviceMessageEventImpl.
 */
@ToString
public class DeviceMessageEventImpl extends IgniteEventImpl {

    private static final long serialVersionUID = 1L;

    /**
     * Domain.
     */
    private String domain;

    /**
     * Command.
     */
    private String command;

    /**
     * DeviceMessageData.
     */
    private DeviceMessageData data;

    /**
     * Get domain.
     *
     * @return string
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Set domain.
     *
     * @param domain domain
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * Get command.
     *
     * @return string
     */
    public String getCommand() {
        return command;
    }

    /**
     * Set command.
     *
     * @param command string
     */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * Get data.
     *
     * @return DeviceMessageData
     */
    public DeviceMessageData getData() {
        return data;
    }

    /**
     * Set data.
     *
     * @param data DeviceMessageData
     */
    public void setData(DeviceMessageData data) {
        this.data = data;
    }

    /**
     * Get serieal version UID.
     *
     * @return long
     */
    public static long getSerialversionuid() {
        return serialVersionUID;
    }



}
