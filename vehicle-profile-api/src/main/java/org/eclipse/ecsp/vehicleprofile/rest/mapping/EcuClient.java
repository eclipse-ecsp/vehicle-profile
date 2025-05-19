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

/**
 * EcuClient class.
 */
public class EcuClient {

    public EcuClient() {
        super();
    }

    /**
     * EcuClient constructor.
     *
     * @param ecu string
     * @param clientId  string
     */
    public EcuClient(String ecu, String clientId) {
        super();
        this.ecu = ecu;
        this.clientId = clientId;
    }

    private String ecu;
    private String clientId;

    /**
     * get ecu.
     *
     * @return string
     */
    public String getEcu() {
        return ecu;
    }

    /**
     * set ecu.
     *
     * @param ecu string
     */
    public void setEcu(String ecu) {
        this.ecu = ecu;
    }

    /**
     * get client id
     *
     * @return string
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Set client id
     *
     * @param clientId string
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

}
