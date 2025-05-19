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

package org.eclipse.ecsp.vehicleprofile.domain;

import dev.morphia.annotations.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * VehicleAttributes.
 */
@Entity
@EqualsAndHashCode
@Setter
@Getter
public class VehicleAttributes {
    private String make;
    private String model;
    private String marketingColor;
    private String baseColor;
    private String modelYear;
    private String destinationCountry;
    private String engineType;
    private String bodyStyle;
    private String bodyType;
    private String name;
    private String trim;
    private String type;
    private Integer fuelType;

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "VehicleAttributes [" + (make != null ? "make=" + make + ", " : "")
                + (model != null ? "model=" + model + ", " : "")
                + (marketingColor != null ? "marketingColor=" + marketingColor + ", " : "")
                + (baseColor != null ? "baseColor=" + baseColor + ", " : "")
                + (modelYear != null ? "modelYear=" + modelYear + ", " : "")
                + (destinationCountry != null ? "destinationCountry=" + destinationCountry + ", " : "")
                + (engineType != null ? "engineType=" + engineType + ", " : "")
                + (bodyStyle != null ? "bodyStyle=" + bodyStyle + ", " : "")
                + (bodyType != null ? "bodyType=" + bodyType + ", " : "") + (name != null ? "name=" + name + ", " : "")
                + (trim != null ? "trim=" + trim + ", " : "") + (type != null ? "type=" + type + ", " : "")
                + (fuelType != null ? "fuelType=" + fuelType + ", " : "")
                + (super.toString() != null ? "toString()=" + super.toString() : "") + "]";
    }
}
