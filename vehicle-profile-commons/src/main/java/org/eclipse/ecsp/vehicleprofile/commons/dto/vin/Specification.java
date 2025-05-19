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

package org.eclipse.ecsp.vehicleprofile.commons.dto.vin;

import java.io.Serializable;

/**
 * Specification.
 *
 */
public class Specification implements Serializable {

    private String country;
    private String modelCode;
    private String modelName;
    private String manufacture;

    /**
     * Specification constructor.
     */
    public Specification() {
        super();
    }

    /**
     * Specification constructor.
     *
     * @param country      the country
     * @param modelCode    the model code
     * @param modelName    the model name
     * @param manufacture  the manufacture
     */
    public Specification(String country, String modelCode, String modelName, String manufacture) {
        this.country = country;
        this.modelCode = modelCode;
        this.modelName = modelName;
        this.manufacture = manufacture;
    }

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
     * Get the model code.
     *
     * @return the model code
     */
    public String getModelCode() {
        return modelCode;
    }

    /**
     * Sets the model code.
     *
     * @param modelCode the new model code
     */
    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }

    /**
     * Get the model name.
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

    @Override
    public String toString() {
        return "Specification{" + "country='" + country + '\'' + ", modelCode='" + modelCode + '\'' + ", modelName='"
                + modelName + '\'' + ", manufacture='" + manufacture + '\'' + '}';
    }
}
