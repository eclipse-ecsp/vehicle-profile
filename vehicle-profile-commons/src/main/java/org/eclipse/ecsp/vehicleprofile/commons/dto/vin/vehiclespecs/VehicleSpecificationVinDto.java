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

package org.eclipse.ecsp.vehicleprofile.commons.dto.vin.vehiclespecs;

import org.eclipse.ecsp.vehicleprofile.commons.dto.vin.VinDto;

/**
 * VehicleSpecificationVinDto.
 *
 */
public class VehicleSpecificationVinDto extends VinDto {
    private String vehicleSpecificationAuthBaseUrl;

    private String vehicleSpecificationAuthTokenUrl;

    private String vehicleSpecificationAuthTokenAudience;

    private String vehicleSpecificationAuthTokenGrantType;

    private String vehicleSpecificationVinDecodeBaseUrl;

    private String vehicleSpecificationVinDecodeUrl;

    private String vehicleSpecificationClientId;

    private String vehicleSpecificationClientSecret;

    /**
     * Get vehicle specification auth base url.
     */
    public String getVehicleSpecificationAuthBaseUrl() {
        return vehicleSpecificationAuthBaseUrl;
    }

    /**
     * Set the vehicle specification auth base url.
     *
     * @param vehicleSpecificationAuthBaseUrl string
     */
    public void setVehicleSpecificationAuthBaseUrl(String vehicleSpecificationAuthBaseUrl) {
        this.vehicleSpecificationAuthBaseUrl = vehicleSpecificationAuthBaseUrl;
    }

    /**
     * Get the vehicle specification auth token url.
     *
     * @return string
     */
    public String getVehicleSpecificationAuthTokenUrl() {
        return vehicleSpecificationAuthTokenUrl;
    }

    /**
     * Set the vehicle specification auth token url.
     *
     * @param vehicleSpecificationAuthTokenUrl string
     */
    public void setVehicleSpecificationAuthTokenUrl(String vehicleSpecificationAuthTokenUrl) {
        this.vehicleSpecificationAuthTokenUrl = vehicleSpecificationAuthTokenUrl;
    }

    /**
     * Get the vehicle specification auth token audience.
     *
     * @return string
     */
    public String getVehicleSpecificationAuthTokenAudience() {
        return vehicleSpecificationAuthTokenAudience;
    }

    /**
     * Set the vehicle specification auth token audience.
     *
     * @param vehicleSpecificationAuthTokenAudience string
     */
    public void setVehicleSpecificationAuthTokenAudience(String vehicleSpecificationAuthTokenAudience) {
        this.vehicleSpecificationAuthTokenAudience = vehicleSpecificationAuthTokenAudience;
    }

    /**
     * Get the vehicle specification auth token grant type.
     *
     * @return string
     */
    public String getVehicleSpecificationAuthTokenGrantType() {
        return vehicleSpecificationAuthTokenGrantType;
    }

    /**
     * Set the vehicle specification auth token grant type.
     *
     * @param vehicleSpecificationAuthTokenGrantType string
     */
    public void setVehicleSpecificationAuthTokenGrantType(String vehicleSpecificationAuthTokenGrantType) {
        this.vehicleSpecificationAuthTokenGrantType = vehicleSpecificationAuthTokenGrantType;
    }

    /**
     * Get the vehicle specification vin decode base url.
     *
     * @return string
     */
    public String getVehicleSpecificationVinDecodeBaseUrl() {
        return vehicleSpecificationVinDecodeBaseUrl;
    }

    /**
     * Set the vehicle specification vin decode base url.
     *
     * @param vehicleSpecificationVinDecodeBaseUrl string
     */
    public void setVehicleSpecificationVinDecodeBaseUrl(String vehicleSpecificationVinDecodeBaseUrl) {
        this.vehicleSpecificationVinDecodeBaseUrl = vehicleSpecificationVinDecodeBaseUrl;
    }

    /**
     * Get the vehicle specification vin decode url.
     *
     * @return string
     */
    public String getVehicleSpecificationVinDecodeUrl() {
        return vehicleSpecificationVinDecodeUrl;
    }

    /**
     * Set the vehicle specification vin decode url.
     *
     * @param vehicleSpecificationVinDecodeUrl string
     */
    public void setVehicleSpecificationVinDecodeUrl(String vehicleSpecificationVinDecodeUrl) {
        this.vehicleSpecificationVinDecodeUrl = vehicleSpecificationVinDecodeUrl;
    }

    /**
     * Get the vehicle specification client id.
     *
     * @return string
     */
    public String getVehicleSpecificationClientId() {
        return vehicleSpecificationClientId;
    }

    /**
     * Set the vehicle specification client id.
     *
     * @param vehicleSpecificationClientId string
     */
    public void setVehicleSpecificationClientId(String vehicleSpecificationClientId) {
        this.vehicleSpecificationClientId = vehicleSpecificationClientId;
    }

    /**
     * Get the vehicle specification client secret.
     *
     * @return string
     */
    public String getVehicleSpecificationClientSecret() {
        return vehicleSpecificationClientSecret;
    }

    /**
     * Set the vehicle specification client secret.
     *
     * @param vehicleSpecificationClientSecret string
     */
    public void setVehicleSpecificationClientSecret(String vehicleSpecificationClientSecret) {
        this.vehicleSpecificationClientSecret = vehicleSpecificationClientSecret;
    }

    @Override
    public String toString() {
        return "VehicleSpecificationVinDto{" + "vehicleSpecificationAuthBaseUrl='" + vehicleSpecificationAuthBaseUrl
                + '\'' + ", vehicleSpecificationAuthTokenUrl='" + vehicleSpecificationAuthTokenUrl + '\''
                + ", vehicleSpecificationAuthTokenAudience='" + vehicleSpecificationAuthTokenAudience + '\''
                + ", vehicleSpecificationAuthTokenGrantType='" + vehicleSpecificationAuthTokenGrantType + '\''
                + ", vehicleSpecificationVinDecodeBaseUrl='" + vehicleSpecificationVinDecodeBaseUrl + '\''
                + ", vehicleSpecificationVinDecodeUrl='" + vehicleSpecificationVinDecodeUrl + '\''
                + ", vehicleSpecificationClientId='" + vehicleSpecificationClientId + '\''
                + ", vehicleSpecificationClientSecret='" + vehicleSpecificationClientSecret + '\'' + ", vin='" + vin
                + '\'' + '}';
    }
}
