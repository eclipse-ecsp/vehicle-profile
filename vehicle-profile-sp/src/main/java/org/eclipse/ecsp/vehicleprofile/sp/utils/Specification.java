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

package org.eclipse.ecsp.vehicleprofile.sp.utils;

/**
 * Specification class.
 */
public class Specification {

    private String country;
    private String modelType;
    private String manufacture;

    /**
     * Get the country.
     *
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the country.
     *
     * @param country the new country
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Get the modelType.
     *
     * @return the modelType
     */
    public String getModelType() {
        return modelType;
    }

    /**
     * Sets the modelType.
     *
     * @param modelType the new modelType
     */
    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    /**
     * Get the manufacture.
     *
     * @return the manufacture
     */
    public String getManufacture() {
        return manufacture;
    }

    /**
     * Sets the manufacture.
     *
     * @param manufacture the new manufacture
     */
    public void setManufacture(String manufacture) {
        this.manufacture = manufacture;
    }

}
